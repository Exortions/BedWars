package org.screamingsandals.bedwars.game;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.screamingsandals.bedwars.api.game.GameManager;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.variants.VariantManagerImpl;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.methods.OnPreDisable;
import org.screamingsandals.lib.utils.annotations.parameters.DataFolder;
import org.screamingsandals.lib.utils.logger.LoggerWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service(dependsOn = {
        VariantManagerImpl.class // it's important to have variant manager loaded before games manager
})
@RequiredArgsConstructor
public class GameManagerImpl implements GameManager<GameImpl> {
    @DataFolder("arenas")
    private final Path arenasFolder;
    private final LoggerWrapper logger;
    private final List<GameImpl> games = new LinkedList<>();

    public static GameManagerImpl getInstance() {
        return ServiceManager.get(GameManagerImpl.class);
    }

    @Override
    public Optional<GameImpl> getGame(String name) {
        try {
            var uuid = UUID.fromString(name);
            return getGame(uuid);
        } catch (Throwable ignored) {
            return games.stream().filter(game -> game.getName().equals(name)).findFirst();
        }
    }

    @Override
    public Optional<GameImpl> getGame(UUID uuid) {
        return games.stream().filter(game -> game.getUuid().equals(uuid)).findFirst();
    }

    @Override
    public List<GameImpl> getGames() {
        return List.copyOf(games);
    }

    @Override
    public List<String> getGameNames() {
        return games.stream().map(GameImpl::getName).collect(Collectors.toList());
    }

    @Override
    public boolean hasGame(String name) {
        return getGame(name).isPresent();
    }

    @Override
    public boolean hasGame(UUID uuid) {
        return getGame(uuid).isPresent();
    }

    @Override
    public Optional<GameImpl> getGameWithHighestPlayers() {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .filter(game -> game.countConnectedPlayers() < game.getMaxPlayers())
                .max(Comparator.comparingInt(GameImpl::countConnectedPlayers));
    }

    @Override
    public Optional<GameImpl> getGameWithLowestPlayers() {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .filter(game -> game.countConnectedPlayers() < game.getMaxPlayers())
                .min(Comparator.comparingInt(GameImpl::countConnectedPlayers));
    }

    @Override
    public Optional<GameImpl> getFirstWaitingGame() {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.WAITING)
                .max(Comparator.comparingInt(GameImpl::countConnectedPlayers));
    }

    @Override
    public Optional<GameImpl> getFirstRunningGame() {
        return games.stream()
                .filter(game -> game.getStatus() == GameStatus.RUNNING || game.getStatus() == GameStatus.GAME_END_CELEBRATING)
                .max(Comparator.comparingInt(GameImpl::countConnectedPlayers));
    }

    public void addGame(@NotNull GameImpl game) {
        if (!games.contains(game)) {
            games.add(game);
        }
    }

    public void removeGame(@NotNull GameImpl game) {
        games.remove(game);
    }

    @OnPostEnable
    public void onPostEnable() {
        if (Files.exists(arenasFolder)) {
            try (var stream = Files.walk(arenasFolder.toAbsolutePath())) {
                final var results = stream.filter(Files::isRegularFile)
                        .map(Path::toFile)
                        .collect(Collectors.toList());
                if (results.isEmpty()) {
                    logger.debug("No arenas have been found!");
                } else {
                    results.forEach(file -> {
                        if (file.exists() && file.isFile() && !file.getName().toLowerCase().endsWith(".disabled")) {
                            var game = GameImpl.loadGame(file);
                            if (game != null) {
                                games.add(game);
                            }
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnPreDisable
    public void onPreDisable() {
        games.forEach(GameImpl::stop);
        games.clear();
    }
}

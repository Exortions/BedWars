package org.screamingsandals.bedwars.api.events;

import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;

import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface GameChangedStatusEvent<G extends Game> {
    G getGame();

    @SuppressWarnings("unchecked")
    static void handle(Object plugin, Consumer<GameChangedStatusEvent<Game>> consumer) {
        BedwarsAPI.getInstance().getEventUtils().handle(plugin, GameChangedStatusEvent.class, (Consumer) consumer);
    }
}

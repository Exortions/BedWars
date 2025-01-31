package org.screamingsandals.bedwars.api.events;

import org.jetbrains.annotations.ApiStatus;
import org.screamingsandals.bedwars.api.BedwarsAPI;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.ItemSpawner;
import org.screamingsandals.bedwars.api.game.ItemSpawnerType;
import org.screamingsandals.lib.utils.Wrapper;

import java.util.function.Consumer;

@ApiStatus.NonExtendable
public interface ResourceSpawnEvent<G extends Game, S extends ItemSpawner, T extends ItemSpawnerType, I extends Wrapper, L extends Wrapper> extends BWCancellable {
    G getGame();

    S getItemSpawner();

    L getLocation();

    I getResource();

    T getType();

    /**
     *
     * @param resource wrapper or platform ItemStack
     */
    void setResource(Object resource);

    @SuppressWarnings("unchecked")
    static void handle(Object plugin, Consumer<ResourceSpawnEvent<Game, ItemSpawner, ItemSpawnerType, Wrapper, Wrapper>> consumer) {
        BedwarsAPI.getInstance().getEventUtils().handle(plugin, ResourceSpawnEvent.class, (Consumer) consumer);
    }
}

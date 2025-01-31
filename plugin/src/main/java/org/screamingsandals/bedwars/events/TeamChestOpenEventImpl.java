package org.screamingsandals.bedwars.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.screamingsandals.bedwars.api.events.TeamChestOpenEvent;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.TeamImpl;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.lib.event.CancellableAbstractEvent;

@EqualsAndHashCode(callSuper = true)
@Data
public class TeamChestOpenEventImpl extends CancellableAbstractEvent implements TeamChestOpenEvent<GameImpl, BedWarsPlayer, TeamImpl> {
    private final GameImpl game;
    private final BedWarsPlayer player;
    private final TeamImpl team;
}

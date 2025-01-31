package org.screamingsandals.bedwars.utils;

import lombok.Data;
import org.screamingsandals.bedwars.api.special.SpecialItem;
import org.screamingsandals.bedwars.api.utils.DelayFactory;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.lib.tasker.Tasker;
import org.screamingsandals.lib.tasker.TaskerTime;
import org.screamingsandals.lib.tasker.task.TaskerTask;

@Data
public class DelayFactoryImpl implements DelayFactory {
    private int remainDelay;
    private final SpecialItem specialItem;
    private final BedWarsPlayer player;
    private final GameImpl game;
    private boolean delayActive;
    private TaskerTask task;

    public DelayFactoryImpl(int remainDelay, SpecialItem specialItem, BedWarsPlayer player, GameImpl game) {
        this.remainDelay = remainDelay;
        this.specialItem = specialItem;
        this.player = player;
        this.game = game;

        runDelay();
    }

    private void runDelay() {
        this.task = Tasker.
                build(() -> {
                    if (remainDelay > 0) {
                        delayActive = true;
                        remainDelay--;
                        if (remainDelay == 0) {
                            delayActive = false;

                            this.task.cancel();
                            game.unregisterDelay(this);
                        }
                    }
                })
                .repeat(20, TaskerTime.TICKS)
                .start();
    }
}

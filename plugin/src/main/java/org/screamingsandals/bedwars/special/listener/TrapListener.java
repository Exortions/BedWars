package org.screamingsandals.bedwars.special.listener;

import org.screamingsandals.bedwars.utils.ItemUtils;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.events.PlayerBreakBlockEventImpl;
import org.screamingsandals.bedwars.events.PlayerBuildBlockEventImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.PlayerManagerImpl;
import org.screamingsandals.bedwars.special.TrapImpl;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.player.SPlayerMoveEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.List;
import java.util.Map;

@Service
public class TrapListener {
    private static final String TRAP_PREFIX = "Module:Trap:";

    @OnEvent
    public void onTrapRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("trap")) {
            TrapImpl trap = new TrapImpl(event.getGame(), event.getPlayer(),
                    event.getGame().getPlayerTeam(event.getPlayer()),
                    (List<Map<String, Object>>) event.getProperty("data"));

            int id = System.identityHashCode(trap);
            String trapString = TRAP_PREFIX + id;

            ItemUtils.saveData(event.getStack(), trapString);
        }

    }

    @OnEvent
    public void onTrapBuild(PlayerBuildBlockEventImpl event) {
        if (event.isCancelled()) {
            return;
        }

        var trapItem = event.getItemInHand();
        String unhidden = ItemUtils.getIfStartsWith(trapItem, TRAP_PREFIX);
        if (unhidden != null) {
            int classID = Integer.parseInt(unhidden.split(":")[2]);

            for (var special : event.getGame().getActiveSpecialItems(TrapImpl.class)) {
                if (System.identityHashCode(special) == classID) {
                    special.place(event.getBlock().getLocation());
                    event.getPlayer().sendMessage(Message.of(LangKeys.SPECIALS_TRAP_BUILT).prefixOrDefault((event.getGame()).getCustomPrefixComponent()));
                    return;
                }
            }
        }

    }

    @OnEvent
    public void onTrapBreak(PlayerBreakBlockEventImpl event) {
        for (var special : event.getGame().getActiveSpecialItems(TrapImpl.class)) {
            var runningTeam = event.getTeam();

            if (special.isPlaced()
                    && event.getBlock().getLocation().equals(special.getLocation())) {
                event.setDrops(false);
                special.process(event.getPlayer(), runningTeam, true);
            }
        }
    }

    @OnEvent
    public void onMove(SPlayerMoveEvent event) {
        var player = event.getPlayer();
        if (event.isCancelled() || !PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var difX = Math.abs(event.getCurrentLocation().getX() - event.getNewLocation().getX());
        var difZ = Math.abs(event.getCurrentLocation().getZ() - event.getNewLocation().getZ());

        if (difX == 0.0 && difZ == 0.0) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();
        if (game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator()) {
            for (var special : game.getActiveSpecialItems(TrapImpl.class)) {

                if (special.isPlaced()) {
                    if (game.getPlayerTeam(gPlayer) != special.getTeam()) {
                        if (event.getNewLocation().getBlock().equals(special.getLocation().getBlock())) {
                            special.process(gPlayer, game.getPlayerTeam(gPlayer), false);
                        }
                    }
                }
            }
        }
    }
}

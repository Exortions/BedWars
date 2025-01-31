package org.screamingsandals.bedwars.special.listener;

import org.screamingsandals.bedwars.utils.ItemUtils;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.player.PlayerManagerImpl;
import org.screamingsandals.bedwars.special.WarpPowderImpl;
import org.screamingsandals.bedwars.utils.DelayFactoryImpl;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.lib.entity.EntityHuman;
import org.screamingsandals.lib.entity.EntityItem;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.event.entity.SEntityDamageEvent;
import org.screamingsandals.lib.event.player.SPlayerInteractEvent;
import org.screamingsandals.lib.event.player.SPlayerMoveEvent;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.utils.BlockFace;
import org.screamingsandals.lib.utils.annotations.Service;

@Service
public class WarpPowderListener {
    private static final String WARP_POWDER_PREFIX = "Module:WarpPowder:";

    @OnEvent
    public void onPowderItemRegister(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("warppowder")) {
            ItemUtils.saveData(event.getStack(), applyProperty(event));
        }
    }

    @OnEvent
    public void onPlayerUseItem(SPlayerInteractEvent event) {
        var player = event.getPlayer();
        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();
        if (event.getAction() == SPlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.getAction() == SPlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            if (game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator()) {
                if (event.getItem() != null) {
                    var stack = event.getItem();
                    var unhidden = ItemUtils.getIfStartsWith(stack, WARP_POWDER_PREFIX);

                    if (unhidden != null) {
                        event.setCancelled(true);
                        if (!game.isDelayActive(gPlayer, WarpPowderImpl.class)) {
                            int teleportTime = Integer.parseInt(unhidden.split(":")[2]);
                            int delay = Integer.parseInt(unhidden.split(":")[3]);
                            var warpPowder = new WarpPowderImpl(game, gPlayer, game.getPlayerTeam(gPlayer), stack, teleportTime);

                            if (event.getPlayer().getLocation().add(BlockFace.DOWN.getDirection()).getBlock().getType().isAir()) {
                                return;
                            }

                            if (delay > 0) {
                                var delayFactory = new DelayFactoryImpl(delay, warpPowder, gPlayer, game);
                                game.registerDelay(delayFactory);
                            }

                            warpPowder.runTask();
                        } else {
                            int delay = game.getActiveDelay(gPlayer, WarpPowderImpl.class).getRemainDelay();
                            MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_ITEM_DELAY).placeholder("time", delay));
                        }
                    }
                }
            }
        }
    }


    @OnEvent
    public void onDamage(SEntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof EntityHuman)) {
            return;
        }

        var player = ((EntityHuman) event.getEntity()).asPlayer();

        if (!PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        var gPlayer = PlayerManagerImpl.getInstance().getPlayer(player).orElseThrow();
        var game = gPlayer.getGame();

        if (gPlayer.isSpectator()) {
            return;
        }

        var warpPowder = game.getFirstActiveSpecialItemOfPlayer(gPlayer, WarpPowderImpl.class);
        if (warpPowder != null) {
            warpPowder.cancelTeleport(false, true);
        }
    }

    @OnEvent
    public void onMove(SPlayerMoveEvent event) {
        var player = event.getPlayer();
        if (event.isCancelled() || !PlayerManagerImpl.getInstance().isPlayerInGame(player)) {
            return;
        }

        if (event.getCurrentLocation().getX() == event.getNewLocation().getX()
                && event.getCurrentLocation().getY() == event.getNewLocation().getY()
                && event.getCurrentLocation().getZ() == event.getNewLocation().getZ()) {
            return;
        }

        var gPlayer = player.as(BedWarsPlayer.class);
        var game = gPlayer.getGame();
        if (gPlayer.isSpectator()) {
            return;
        }

        var warpPowder = game.getFirstActiveSpecialItemOfPlayer(gPlayer, WarpPowderImpl.class);
        if (warpPowder != null) {
            warpPowder.cancelTeleport(true, true);

            if (player.getPlayerInventory().firstEmptySlot() == -1 && !player.getPlayerInventory().contains(warpPowder.getItem())) {
                EntityItem.dropItem(warpPowder.getItem(), player.getLocation());
            } else {
                player.getPlayerInventory().addItem(warpPowder.getItem());
            }
            player.forceUpdateInventory();
        }
    }

    private String applyProperty(ApplyPropertyToBoughtItemEventImpl event) {
        return WARP_POWDER_PREFIX
                + MiscUtils.getIntFromProperty(
                "teleport-time", "specials.warp-powder.teleport-time", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.warp-powder.delay", event);
    }
}

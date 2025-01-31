package org.screamingsandals.bedwars.special.listener;

import org.screamingsandals.bedwars.utils.ItemUtils;
import org.screamingsandals.bedwars.api.special.SpecialItem;
import org.screamingsandals.bedwars.events.ApplyPropertyToBoughtItemEventImpl;
import org.screamingsandals.bedwars.events.PlayerBreakBlockEventImpl;
import org.screamingsandals.bedwars.events.PlayerBuildBlockEventImpl;
import org.screamingsandals.bedwars.special.LuckyBlockImpl;
import org.screamingsandals.lib.event.OnEvent;
import org.screamingsandals.lib.utils.annotations.Service;

import java.util.List;
import java.util.Map;

@Service
public class LuckyBlockAddonListener {

    public static final String LUCKY_BLOCK_PREFIX = "Module:LuckyBlock:";

    @OnEvent
    public void onLuckyBlockRegistered(ApplyPropertyToBoughtItemEventImpl event) {
        if (event.getPropertyName().equalsIgnoreCase("luckyblock")) {
            var lucky = new LuckyBlockImpl(event.getGame(), event.getPlayer(),
                    event.getGame().getPlayerTeam(event.getPlayer()),
                    (List<Map<String, Object>>) event.getProperty("data"));

            var id = System.identityHashCode(lucky);

            var luckyBlockString = LUCKY_BLOCK_PREFIX + id;

            ItemUtils.saveData(event.getStack(), luckyBlockString);
        }
    }

    @OnEvent
    public void onLuckyBlockBuild(PlayerBuildBlockEventImpl event) {
        if (event.isCancelled()) {
            return;
        }

        var luckyItem = event.getItemInHand();
        var invisible = ItemUtils.getIfStartsWith(luckyItem, LUCKY_BLOCK_PREFIX);
        if (invisible != null) {
            var splitted = invisible.split(":");
            var classID = Integer.parseInt(splitted[2]);

            for (SpecialItem special : event.getGame().getActiveSpecialItems(LuckyBlockImpl.class)) {
                var luckyBlock = (LuckyBlockImpl) special;
                if (System.identityHashCode(luckyBlock) == classID) {
                    luckyBlock.place(event.getBlock().getLocation());
                    return;
                }
            }
        }

    }

    @OnEvent
    public void onLuckyBlockBreak(PlayerBreakBlockEventImpl event) {
        if (event.isCancelled()) {
            return;
        }
        for (var special : event.getGame().getActiveSpecialItems(LuckyBlockImpl.class)) {
            var luckyBlock = (LuckyBlockImpl) special;
            if (luckyBlock.isPlaced()) {
                if (event.getBlock().getLocation().equals(luckyBlock.getBlockLocation())) {
                    event.setDrops(false);
                    luckyBlock.process(event.getPlayer());
                    return;
                }
            }
        }
    }

}

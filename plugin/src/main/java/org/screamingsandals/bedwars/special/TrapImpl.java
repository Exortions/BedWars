package org.screamingsandals.bedwars.special;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.screamingsandals.bedwars.api.special.Trap;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.TeamImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.bedwars.player.BedWarsPlayer;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.screamingsandals.bedwars.utils.Sounds;
import org.screamingsandals.lib.block.BlockTypeHolder;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.item.meta.PotionEffectHolder;
import org.screamingsandals.lib.world.LocationHolder;

import java.util.List;
import java.util.Map;

@Getter
@EqualsAndHashCode(callSuper = true)
public class TrapImpl extends SpecialItem implements Trap<GameImpl, BedWarsPlayer, TeamImpl, LocationHolder> {
    private final List<Map<String, Object>> trapData;
    private LocationHolder location;

    public TrapImpl(GameImpl game, BedWarsPlayer player, TeamImpl team, List<Map<String, Object>> trapData) {
        super(game, player, team);
        this.trapData = trapData;

        game.registerSpecialItem(this);
    }

    @Override
    public boolean isPlaced() {
        return location != null;
    }

    public void place(LocationHolder loc) {
        this.location = loc;
    }

    public void process(BedWarsPlayer player, TeamImpl runningTeam, boolean forceDestroy) {
        if (runningTeam == this.team || forceDestroy) {
            game.unregisterSpecialItem(this);
            location.getBlock().setType(BlockTypeHolder.air());
            return;
        }

        for (var data : trapData) {
            if (data.containsKey("sound")) {
                var sound = (String) data.get("sound");
                Sounds.playSound(player, location, sound, null, 1, 1);
            }

            if (data.containsKey("effect")) {
                PotionEffectHolder.ofOptional(data.get("effect")).ifPresent(effect -> player.asEntity().addPotionEffect(effect));
            }

            if (data.containsKey("damage")) {
                var damage = (double) data.get("damage");
                player.asEntity().damage(damage);
            }
        }

        for (var p : this.team.getPlayers()) {
            MiscUtils.sendActionBarMessage(p, Message.of(LangKeys.SPECIALS_TRAP_CAUGHT_TEAM).placeholder("player", player.getDisplayName()));
        }
        MiscUtils.sendActionBarMessage(player, Message.of(LangKeys.SPECIALS_TRAP_CAUGHT).placeholder("team", getTeam().getName()));
    }
}

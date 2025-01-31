package org.screamingsandals.bedwars.region;

import org.screamingsandals.lib.utils.BlockFace;
import org.screamingsandals.lib.block.BlockHolder;

public class LegacyBedUtils {
    public static BlockHolder getBedNeighbor(BlockHolder head) {
        if (isBedBlock(head.getLocation().add(BlockFace.EAST.getDirection()).getBlock())) {
            return head.getLocation().add(BlockFace.EAST.getDirection()).getBlock();
        } else if (isBedBlock(head.getLocation().add(BlockFace.WEST.getDirection()).getBlock())) {
            return head.getLocation().add(BlockFace.WEST.getDirection()).getBlock();
        } else if (isBedBlock(head.getLocation().add(BlockFace.SOUTH.getDirection()).getBlock())) {
            return head.getLocation().add(BlockFace.SOUTH.getDirection()).getBlock();
        } else {
            return head.getLocation().add(BlockFace.NORTH.getDirection()).getBlock();
        }
    }

    public static boolean isBedBlock(BlockHolder isBed) {
        if (isBed == null) {
            return false;
        }

        return isBed.getType().isSameType("bed", "bed_block");
    }
}

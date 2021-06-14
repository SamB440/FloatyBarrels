package com.convallyria.floatybarrels.utils;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;

public class BlockUtils {

    public static Pair<Block, Block> getLeftAndRightBlocks(Player observer, Block relativeBlock) {
        BlockFace facing = observer.getFacing();
        Block leftBlock, rightBlock;
        if (facing == BlockFace.NORTH) {
            leftBlock = relativeBlock.getRelative(BlockFace.WEST);
            rightBlock = relativeBlock.getRelative(BlockFace.EAST);
        } else if (facing == BlockFace.EAST) {
            leftBlock = relativeBlock.getRelative(BlockFace.NORTH);
            rightBlock = relativeBlock.getRelative(BlockFace.SOUTH);
        } else if (facing == BlockFace.SOUTH) {
            leftBlock = relativeBlock.getRelative(BlockFace.EAST);
            rightBlock = relativeBlock.getRelative(BlockFace.WEST);
        } else if (facing == BlockFace.WEST) {
            leftBlock = relativeBlock.getRelative(BlockFace.SOUTH);
            rightBlock = relativeBlock.getRelative(BlockFace.NORTH);
        } else {
            leftBlock = relativeBlock;
            rightBlock = relativeBlock;
        }

        return Pair.of(leftBlock, rightBlock);
    }
}

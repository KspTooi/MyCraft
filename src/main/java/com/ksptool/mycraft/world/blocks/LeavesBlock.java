package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.sharedcore.world.BlockState;
import com.ksptool.mycraft.sharedcore.BlockType;

/**
 * 树叶方块类
 */
public class LeavesBlock extends Block {
    public LeavesBlock() {
        super(BlockType.LEAVES.getNamespacedId(), 0.2f, 0);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        return "leaves_oak.png";
    }
}

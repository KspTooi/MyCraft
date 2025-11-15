package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.sharedcore.world.BlockState;
import com.ksptool.mycraft.sharedcore.BlockType;

/**
 * 石头方块类
 */
public class StoneBlock extends Block {
    public StoneBlock() {
        super(BlockType.STONE.getNamespacedId(), 1.5f, 1);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        return "stone.png";
    }
}

package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.world.BlockState;
import com.ksptool.mycraft.commons.BlockType;

/**
 * 泥土方块类
 */
public class DirtBlock extends Block {

    public DirtBlock() {
        super(BlockType.DIRT.getNamespacedId(), 0.5f, 0);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        return "dirt.png";
    }
}

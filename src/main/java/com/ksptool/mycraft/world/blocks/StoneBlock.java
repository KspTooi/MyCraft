package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.world.BlockState;

/**
 * 石头方块类
 */
public class StoneBlock extends Block {
    public StoneBlock() {
        super("mycraft:stone", 1.5f, 1);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        return "stone.png";
    }
}

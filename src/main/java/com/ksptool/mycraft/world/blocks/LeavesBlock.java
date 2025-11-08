package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.world.BlockState;

public class LeavesBlock extends Block {
    public LeavesBlock() {
        super("mycraft:leaves", 0.2f, 0);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        return "leaves_oak.png";
    }
}

package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.world.BlockState;
import com.ksptool.mycraft.world.properties.BooleanProperty;

/**
 * 草方块类，具有雪化属性
 */
public class GrassBlock extends Block {
    public static final BooleanProperty SNOWY = BooleanProperty.create("snowy");

    public GrassBlock() {
        super("mycraft:grass_block", 0.6f, 0);
    }

    @Override
    protected void defineProperties() {
        addProperty(SNOWY);
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        if (face == 0) {
            return "grass_top.png";
        }
        if (face == 1) {
            return "dirt.png";
        }
        return "grass_side.png";
    }
}

package com.ksptool.mycraft.world.blocks;

import com.ksptool.mycraft.world.Block;
import com.ksptool.mycraft.world.BlockState;
import com.ksptool.mycraft.world.properties.EnumProperty;

/**
 * 木头方块类，具有方向轴属性
 */
public class WoodBlock extends Block {
    public static final EnumProperty<Axis> AXIS = EnumProperty.create("axis", Axis.class);

    public enum Axis {
        X, Y, Z
    }

    public WoodBlock() {
        super("mycraft:wood", 2.0f, 0);
    }

    @Override
    protected void defineProperties() {
        addProperty(AXIS);
    }

    @Override
    public String getTextureName(int face, BlockState state) {
        Axis axis = state.get(AXIS);
        if (axis == Axis.Y) {
            if (face == 0 || face == 1) {
                return "log_oak_top.png";
            }
            return "log_oak.png";
        }
        return "log_oak.png";
    }
}

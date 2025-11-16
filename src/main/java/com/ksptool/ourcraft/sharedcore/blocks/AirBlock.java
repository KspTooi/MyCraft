package com.ksptool.ourcraft.sharedcore.blocks;

import com.ksptool.ourcraft.sharedcore.block.SharedBlock;
import com.ksptool.ourcraft.sharedcore.world.BlockState;
import com.ksptool.ourcraft.sharedcore.world.SharedWorld;
import com.ksptool.ourcraft.sharedcore.BlockType;

/**
 * 空气方块类，表示空方块
 */
public class AirBlock extends SharedBlock {

    public AirBlock() {
        super(BlockType.AIR.getNamespacedId(), 0, 0);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(SharedWorld world, int face, BlockState state) {
        if (world.isServerSide()) {
            return null;
        }
        return null;
    }

    @Override
    public boolean isSolid() {
        return false;
    }
}

package com.ksptool.ourcraft.sharedcore.blocks;

import com.ksptool.ourcraft.sharedcore.block.SharedBlock;
import com.ksptool.ourcraft.sharedcore.world.BlockState;
import com.ksptool.ourcraft.sharedcore.world.SharedWorld;
import com.ksptool.ourcraft.sharedcore.BlockType;

/**
 * 石头方块类
 */
public class StoneBlock extends SharedBlock {
    public StoneBlock() {
        super(BlockType.STONE.getNamespacedId(), 1.5f, 1);
    }

    @Override
    protected void defineProperties() {
    }

    @Override
    public String getTextureName(SharedWorld world, int face, BlockState state) {
        if (world.isServerSide()) {
            return null;
        }
        return "stone.png";
    }
}

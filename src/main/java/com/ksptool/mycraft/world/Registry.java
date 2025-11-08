package com.ksptool.mycraft.world;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    private static final Registry INSTANCE = new Registry();
    private final Map<String, Block> blocks;

    private Registry() {
        this.blocks = new HashMap<>();
    }

    public static Registry getInstance() {
        return INSTANCE;
    }

    public void register(Block block) {
        String id = block.getNamespacedID();
        if (blocks.containsKey(id)) {
            throw new IllegalArgumentException("Block with ID " + id + " is already registered!");
        }
        blocks.put(id, block);
    }

    public Block get(String namespacedID) {
        return blocks.get(namespacedID);
    }

    public Map<String, Block> getAllBlocks() {
        return new HashMap<>(blocks);
    }

    public void clear() {
        blocks.clear();
    }
}


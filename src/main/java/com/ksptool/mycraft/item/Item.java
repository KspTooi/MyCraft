package com.ksptool.mycraft.item;

public class Item {
    private int id;
    private String name;
    private int maxStackSize;
    private String blockNamespacedID;

    public Item(int id, String name, int maxStackSize, String blockNamespacedID) {
        this.id = id;
        this.name = name;
        this.maxStackSize = maxStackSize;
        this.blockNamespacedID = blockNamespacedID;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    public String getBlockNamespacedID() {
        return blockNamespacedID;
    }

    public static Item getItem(int id) {
        if (id == 1) return new Item(1, "Grass Block", 64, "mycraft:grass_block");
        if (id == 2) return new Item(2, "Dirt", 64, "mycraft:dirt");
        if (id == 3) return new Item(3, "Stone", 64, "mycraft:stone");
        if (id == 4) return new Item(4, "Wood", 64, "mycraft:wood");
        if (id == 5) return new Item(5, "Leaves", 64, "mycraft:leaves");
        return null;
    }
}


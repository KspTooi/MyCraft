package com.ksptool.ourcraft.client.item;

import com.ksptool.ourcraft.sharedcore.item.ItemStack;
import lombok.Getter;

/**
 * 客户端物品栏管理类，用于显示（非权威）
 * 注意：此类的状态完全由服务端同步，客户端不应有修改物品的逻辑
 */
@Getter
public class ClientInventory {

    private static final int HOTBAR_SIZE = 9;

    private ItemStack[] hotbar;

    private int selectedSlot;

    public ClientInventory() {
        this.hotbar = new ItemStack[HOTBAR_SIZE];
        this.selectedSlot = 0;
        // 注意：客户端不初始化默认物品，所有数据都从服务端同步
    }

    public ItemStack getSelectedItem() {
        if (selectedSlot >= 0 && selectedSlot < HOTBAR_SIZE) {
            ItemStack stack = hotbar[selectedSlot];
            if (stack != null && !stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }

    /**
     * 设置选中的槽位（从服务端同步）
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot < HOTBAR_SIZE) {
            this.selectedSlot = slot;
        }
    }

    /**
     * 更新物品栏（从服务端同步）
     */
    public void updateHotbar(ItemStack[] newHotbar) {
        if (newHotbar != null && newHotbar.length == HOTBAR_SIZE) {
            this.hotbar = newHotbar;
        }
    }
}


package com.vomiter.survivorsdelight.common.container;

import com.vomiter.survivorsdelight.adapter.container.CabinetAdapters;
import com.vomiter.survivorsdelight.registry.SDContainerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class SDCabinetMenu extends AbstractContainerMenu {
    private final Container chest;
    private final int rows;
    public Container getContainer(){return chest;}

    public int getRows(){
        return rows;
    }

    private static Container resolveContainerClient(Level level, BlockPos pos) {
        var be = level.getBlockEntity(pos);
        if (be instanceof Container c) return c;
        // 萬一同步出了問題避免 NPE
        return new SimpleContainer(18); // 做個 18 格的空容器兜底
    }

    public SDCabinetMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(SDContainerTypes.CABINET.get(), id, inv, resolveContainerClient(inv.player.level(), buf.readBlockPos()), 2);
    }

    public SDCabinetMenu(MenuType<?> type, int id, Inventory inv, Container container, int rows) {
        super(type, id);
        this.chest = container;
        this.rows = rows;
        checkContainerSize(container, rows * 9);
        container.startOpen(inv.player);

        // 櫃子槽位（自訂 Slot）
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new SDCabinetSlot(container, c + r * 9, 8 + c * 18, 18 + r * 18));
            }
        }

        int yOff = (rows - 4) * 18;

        // 玩家背包
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                this.addSlot(new Slot(inv, c + r * 9 + 9, 8 + c * 18, 103 + r * 18 + yOff));
            }
        }
        for (int c = 0; c < 9; c++) {
            this.addSlot(new Slot(inv, c, 8 + c * 18, 161 + yOff));
        }
    }

    @Override public boolean stillValid(@NotNull Player player) { return this.chest.stillValid(player); }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.chest.stopOpen(player);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack ret = stack.copy();

        final int containerSlots = this.rows * 9;
        final int hotbarStart = containerSlots + 27;
        final int total = hotbarStart + 9;

        if (index < containerSlots) {
            CabinetAdapters.removeStored(stack);
            if (!this.moveItemStackTo(stack, containerSlots, total, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, ret);
        } else {
            if (!this.moveItemStackTo(stack, 0, containerSlots, false)) {
                if (index < hotbarStart) {
                    if (!this.moveItemStackTo(stack, hotbarStart, total, false)) return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(stack, containerSlots, hotbarStart, false)) return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == ret.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return ret;
    }

    public static class SDCabinetSlot extends Slot {
        SDCabinetBlockEntity cabinet;
        public SDCabinetSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            cabinet = (SDCabinetBlockEntity) container;
        }

        @Override public boolean mayPlace(@NotNull ItemStack stack) {
            return super.mayPlace(stack) && SDCabinetBlockEntity.isValid(stack);
        }

        @Override public void set(@NotNull ItemStack stack) {
            super.set(stack);
            if (!stack.isEmpty()) cabinet.setStored(stack);
        }

        @Override public void setChanged() {
            super.setChanged();
            ItemStack stack = getItem();
            if (!stack.isEmpty()) cabinet.setStored(stack);
        }

        @Override public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            super.onTake(player, stack);
            cabinet.removeStored(stack);
        }

        @Override
        public @NotNull ItemStack remove(int amount) {
            ItemStack out = super.remove(amount);
            if (!out.isEmpty() && this.container instanceof SDCabinetBlockEntity be) {
                assert be.getLevel() != null;
                if (!be.getLevel().isClientSide) {
                    CabinetAdapters.removeStored(out);
                    be.setChanged();
                }
            }
            return out;
        }
    }
}

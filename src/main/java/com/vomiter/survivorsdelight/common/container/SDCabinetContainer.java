package com.vomiter.survivorsdelight.common.container;

import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.container.RestrictedChestContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

//Mostly based on net.dries007.tfc.common.container.RestrictedChestContainer
//dead code

public class SDCabinetContainer extends RestrictedChestContainer {
    private final boolean allowAddSlot;

    public SDCabinetContainer(MenuType<?> type, int id, Inventory inv, Container container, int rows) {
        super(type, id, inv, container, rows);
        checkContainerSize(container, rows * 9);
        this.allowAddSlot = true;

        for(int row = 0; row < rows; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new SDCabinetSlot(container, col + row * 9, 8 + col * 18, 18 + row * 18));
            }
        }

        int yOffset = (rows - 4) * 18;

        for(int row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 103 + row * 18 + yOffset));
            }
        }

        for(int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 161 + yOffset));
        }

    }

    @Override
    protected @NotNull Slot addSlot(@NotNull Slot slot) {
        return this.allowAddSlot ? super.addSlot(slot) : slot;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot instanceof SDCabinetSlot rest) {
            if (slot.hasItem()) {
                ItemStack item = slot.getItem();
                if (!rest.mayPlace(item)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return super.quickMoveStack(player, index);
    }

    /* ================================================================= */

    private static class SDCabinetSlot extends Slot {

        public SDCabinetSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return super.mayPlace(stack) && SDCabinetBlockEntity.isValid(stack);
        }



        @Override
        public void setChanged() {
            super.setChanged();
            ItemStack stack = getItem();
            if (!stack.isEmpty()) {
                FoodCapability.applyTrait(stack, SDFoodTraits.CABINET_STORED);
            }
        }

        @Override
        public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
            super.onTake(player, stack);
            FoodCapability.removeTrait(stack, SDFoodTraits.CABINET_STORED);
        }



    }
}

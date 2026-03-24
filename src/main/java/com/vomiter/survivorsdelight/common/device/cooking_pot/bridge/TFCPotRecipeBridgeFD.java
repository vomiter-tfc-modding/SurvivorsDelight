package com.vomiter.survivorsdelight.common.device.cooking_pot.bridge;

import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.recipes.PotRecipe;
import net.dries007.tfc.common.recipes.SoupPotRecipe;
import net.dries007.tfc.common.recipes.outputs.PotOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class TFCPotRecipeBridgeFD extends SDCookingPotRecipe {

    public TFCPotRecipeBridgeFD(NonNullList<Ingredient> inputItems,
                                ItemStack output, ItemStack container,
                                int cookTime,
                                FluidIngredient fluidIngredient,
                                int fluidAmount
    ) {
        super("", inputItems, output, container, cookTime, 0, fluidIngredient, fluidAmount);
    }

    @Override
    public boolean shouldCalcDynamic() {
        return false;
    }

    public static @Nullable TFCPotRecipeBridgeFD bridge(Level level, IItemHandler items, IFluidHandler fluids) {
        if(level.isClientSide) return null;
        PotRecipe tfc = TFCPotRecipeMatcher.findFirstMatch(level, items, fluids, new int[]{0,1,2,3,4,5}).orElse(null);
        if (tfc == null) return null;

        NonNullList<Ingredient> inputItems = NonNullList.create();
        for (int i = 0; i < Math.min(items.getSlots(), 6); i++) {
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) inputItems.add(Ingredient.of(stack.getItem()));
        }

        PotBlockEntity.PotInventory inv = TFCPotInventorySnapshots.snapshot(items, fluids);
        ItemStack output = getOutputAsItemStack(tfc, inv, level);
        ItemStack container = tfc instanceof SoupPotRecipe? Items.BOWL.getDefaultInstance(): null;
        int cookTime = Math.max(1, (int) (tfc.getDuration() / 5f));
        return new TFCPotRecipeBridgeFD(inputItems, output, container, cookTime, tfc.getFluidIngredient().ingredient(), tfc.getFluidIngredient().amount());
    }

    // ---- PotOutput → ItemStack 的核心轉換 ----
    private static ItemStack getOutputAsItemStack(PotRecipe tfc, PotBlockEntity.PotInventory inv, Level level) {
        if (level.isClientSide) return ItemStack.EMPTY;

        // 1. 湯類
        if (tfc instanceof SoupPotRecipe soupRecipe) {
            PotOutput rawOut = soupRecipe.getOutput(inv);
            if (rawOut instanceof SoupPotRecipe.SoupOutput(ItemStack stack)) {
                return stack.copy();
            }
        }

        // 2. 一般 SimplePotRecipe / 其他 PotRecipe：
        PotOutput out = tfc.getOutput(inv);

        TFCPotInventorySnapshots.DetachedPot offlinePot = new TFCPotInventorySnapshots.DetachedPot();
        PotBlockEntity.PotInventory tempInv = new PotBlockEntity.PotInventory(offlinePot);
        out.onFinish(tempInv);

        ItemStack result = ItemStack.EMPTY;
        for (int slot = 4; slot <= 8; slot++) {
            ItemStack s = tempInv.getStackInSlot(slot);
            if (s.isEmpty()) continue;
            if (result.isEmpty()) {
                result = s.copy();
            } else if (ItemStack.isSameItemSameComponents(result, s)) {
                result.grow(s.getCount());
            }
        }
        return result;
    }
}

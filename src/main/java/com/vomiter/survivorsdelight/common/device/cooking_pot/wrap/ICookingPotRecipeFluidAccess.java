package com.vomiter.survivorsdelight.common.device.cooking_pot.wrap;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import javax.annotation.Nullable;

public interface ICookingPotRecipeFluidAccess {
    FluidStack getFluidInTank();

    /**
     * 與配方需求比對：型態（含 Tag）＋數量
     * @param ingredient 可為 null，null 視為不需流體
     * @param amountMb   需求 mB（<=0 視為不需流體）
     */
    default boolean matchesFluid(@Nullable FluidIngredient ingredient, int amountMb) {
        if (ingredient == null || amountMb <= 0) return true;
        final FluidStack inTank = getFluidInTank();
        if (inTank.isEmpty()) return false;
        // FluidIngredient#test 會處理具體流體或 Tag 的比對
        if (!ingredient.test(inTank)) return false;
        return inTank.getAmount() >= amountMb;
    }
}
package com.vomiter.survivorsdelight.adapter.cooking_pot.fluid;

import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.Nullable;

public interface IFluidRequiringRecipe {
    @Nullable FluidIngredient sdtfc$getFluidIngredient();
    int sdtfc$getRequiredFluidAmount();
    void sdtfc$setFluidRequirement(@Nullable FluidIngredient ing, int amount);
}

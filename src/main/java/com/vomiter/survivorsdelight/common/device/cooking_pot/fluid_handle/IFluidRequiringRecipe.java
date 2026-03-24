package com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle;

import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

public interface IFluidRequiringRecipe {
    @Nullable SizedFluidIngredient sdtfc$getFluidIngredient();
    int sdtfc$getRequiredFluidAmount();
    void sdtfc$setFluidRequirement(@Nullable SizedFluidIngredient ing, int amount);
}

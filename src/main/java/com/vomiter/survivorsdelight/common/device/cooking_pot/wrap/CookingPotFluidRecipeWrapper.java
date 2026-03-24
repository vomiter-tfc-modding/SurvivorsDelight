package com.vomiter.survivorsdelight.common.device.cooking_pot.wrap;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;


public class CookingPotFluidRecipeWrapper extends RecipeWrapper implements ICookingPotRecipeFluidAccess {
    private final FluidStack tank;
    public CookingPotFluidRecipeWrapper(IItemHandler items, FluidStack tankSnapshot) {
        super(items);
        this.tank = tankSnapshot.copy();
    }
    @Override public FluidStack getFluidInTank() { return tank; }
}
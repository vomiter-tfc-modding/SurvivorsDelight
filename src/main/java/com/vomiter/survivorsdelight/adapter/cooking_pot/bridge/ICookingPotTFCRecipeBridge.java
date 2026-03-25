package com.vomiter.survivorsdelight.adapter.cooking_pot.bridge;

import net.minecraft.world.item.crafting.RecipeHolder;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

public interface ICookingPotTFCRecipeBridge {
    RecipeHolder<CookingPotRecipe> sdtfc$getBridgeCached();
    void sdtfc$setBridgeCached(RecipeHolder<CookingPotRecipe> r);
}

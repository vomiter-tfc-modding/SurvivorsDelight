package com.vomiter.survivorsdelight.adapter.cooking_pot;

import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface CookingPotExtraNutrientRule {
    boolean matches(Level level, ItemStack stack, Nutrient nutrient, FoodData data);

    float getValue(Level level, ItemStack stack, Nutrient nutrient, FoodData data);
}
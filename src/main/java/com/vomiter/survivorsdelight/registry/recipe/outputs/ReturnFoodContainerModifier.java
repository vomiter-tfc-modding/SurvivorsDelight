package com.vomiter.survivorsdelight.registry.recipe.outputs;

import com.vomiter.survivorsdelight.registry.SDItemStackModifiers;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifierType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ReturnFoodContainerModifier implements ItemStackModifier {
    public static final ReturnFoodContainerModifier INSTANCE = new ReturnFoodContainerModifier();

    private ReturnFoodContainerModifier() {}

    @Override
    public @NotNull ItemStack apply(@NotNull ItemStack result, @NotNull ItemStack input, @NotNull Context ctx) {
        ItemStack remainder = input.getCraftingRemainingItem();
        return remainder.isEmpty() ? result : remainder;
    }

    @Override
    public boolean dependsOnInput() {
        return true;
    }

    @Override
    public ItemStackModifierType<ReturnFoodContainerModifier> type() {
        return SDItemStackModifiers.RETURN_FOOD_CONTAINER.holder().get();
    }
}

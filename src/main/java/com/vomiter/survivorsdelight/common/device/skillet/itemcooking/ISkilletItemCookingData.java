package com.vomiter.survivorsdelight.common.device.skillet.itemcooking;

import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.registry.ModDataComponents;

public interface ISkilletItemCookingData {
    ItemStack getCooking();
    void setCooking(ItemStack stack);
    float getTargetTemperature();
    void setTargetTemperature(float temperature);
    int getCookingBarWidth();
    void setCookingBarWidth(int width);

    default boolean isCooking() { return !getCooking().isEmpty(); }

    default void clear() {
        setCooking(ItemStack.EMPTY);
        setTargetTemperature(0);
        setCookingBarWidth(0);
        if ((Object) this instanceof ItemStack stack) {
            stack.remove(ModDataComponents.SKILLET_INGREDIENT);
            stack.remove(ModDataComponents.COOKING_TIME_LENGTH);
            stack.remove(ModDataComponents.SKILLET_FLIP_TIMESTAMP);
            stack.remove(ModDataComponents.SKILLET_FLIPPED);
        }
    }
}

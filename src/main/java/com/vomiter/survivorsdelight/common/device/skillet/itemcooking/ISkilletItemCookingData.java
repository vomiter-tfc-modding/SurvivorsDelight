package com.vomiter.survivorsdelight.common.device.skillet.itemcooking;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public interface ISkilletItemCookingData {
    ItemStack getCooking();
    void setCooking(ItemStack stack);
    float getTargetTemperature();
    void setTargetTemperature(float temperature);
    InteractionHand getHand();
    void setHand(InteractionHand hand);
    boolean isCooking();
    void clear();
}

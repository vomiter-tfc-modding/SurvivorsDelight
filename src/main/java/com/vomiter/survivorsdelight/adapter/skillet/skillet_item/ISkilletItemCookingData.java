package com.vomiter.survivorsdelight.adapter.skillet.skillet_item;

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

package com.vomiter.survivorsdelight.common.device.skillet.itemcooking;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SkilletItemCookingData implements ISkilletItemCookingData {
    private ItemStack cooking = ItemStack.EMPTY;
    private float targetTemperature = 0f;
    private InteractionHand hand = InteractionHand.MAIN_HAND;

    @Override public ItemStack getCooking() { return cooking; }
    @Override public void setCooking(ItemStack stack) { this.cooking = stack.copy(); }
    @Override public float getTargetTemperature() { return targetTemperature; }
    @Override public void setTargetTemperature(float temp) { this.targetTemperature = temp; }
    @Override public InteractionHand getHand() { return hand; }
    @Override public void setHand(InteractionHand hand) { this.hand = hand; }
    @Override public boolean isCooking() { return !cooking.isEmpty(); }
    @Override public void clear() { cooking = ItemStack.EMPTY; targetTemperature = 0f; }

    public CompoundTag save(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        if (!cooking.isEmpty()) tag.put("Cooking", cooking.save(lookup));
        tag.putFloat("TargetTemp", targetTemperature);
        tag.putInt("Hand", hand.ordinal());
        return tag;
    }
    public void load(HolderLookup.Provider lookup, CompoundTag tag) {
        this.cooking = tag.contains("Cooking")
                ? ItemStack.parseOptional(lookup, tag.getCompound("Cooking"))
                : ItemStack.EMPTY;
        this.targetTemperature = tag.getFloat("TargetTemp");
        int idx = tag.getInt("Hand");
        this.hand = (idx >= 0 && idx < InteractionHand.values().length) ? InteractionHand.values()[idx] : InteractionHand.MAIN_HAND;
    }
}
package com.vomiter.survivorsdelight.mixin.device.skillet;

import com.vomiter.survivorsdelight.common.device.skillet.itemcooking.ISkilletItemCookingData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class SkilletData_ItemStackMixin implements ISkilletItemCookingData {
    @Unique private ItemStack sdtfc$cookingStack = ItemStack.EMPTY;
    @Unique private float sdtfc$targetTemperature;
    @Unique private int sdtfc$cookingBarWidth;

    @Override public ItemStack getCooking() { return sdtfc$cookingStack; }
    @Override public void setCooking(ItemStack stack) { sdtfc$cookingStack = stack; }
    @Override public float getTargetTemperature() { return sdtfc$targetTemperature; }
    @Override public void setTargetTemperature(float temperature) { sdtfc$targetTemperature = temperature; }
    @Override public int getCookingBarWidth() { return sdtfc$cookingBarWidth; }
    @Override public void setCookingBarWidth(int width) { sdtfc$cookingBarWidth = Math.clamp(width, 0, 13); }
}

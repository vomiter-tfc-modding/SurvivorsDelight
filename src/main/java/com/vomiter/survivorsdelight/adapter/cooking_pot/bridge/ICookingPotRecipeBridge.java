package com.vomiter.survivorsdelight.adapter.cooking_pot.bridge;

import com.vomiter.survivorsdelight.common.device.cooking_pot.bridge.TFCPotRecipeBridgeFD;
import net.minecraft.world.item.ItemStack;

public interface ICookingPotRecipeBridge {
    void sdtfc$setCachedBridge(TFCPotRecipeBridgeFD recipe);
    void sdtfc$setCachedDynamicFoodResult(ItemStack item);
    TFCPotRecipeBridgeFD sdtfc$getCachedBridge();
    ItemStack sdtfc$getCachedDynamicFoodResult();
}

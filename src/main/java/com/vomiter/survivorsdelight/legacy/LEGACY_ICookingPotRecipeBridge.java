package com.vomiter.survivorsdelight.legacy;

import com.vomiter.survivorsdelight.common.device.cooking_pot.bridge.TFCPotRecipeBridgeFD;
import net.minecraft.world.item.ItemStack;

public interface LEGACY_ICookingPotRecipeBridge {
    void sdtfc$setCachedBridge(TFCPotRecipeBridgeFD recipe);
    void sdtfc$setCachedDynamicFoodResult(ItemStack item);
    TFCPotRecipeBridgeFD sdtfc$getCachedBridge();
    ItemStack sdtfc$getCachedDynamicFoodResult();
}

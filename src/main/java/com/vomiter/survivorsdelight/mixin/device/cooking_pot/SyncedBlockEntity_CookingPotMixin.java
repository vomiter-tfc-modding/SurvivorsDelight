package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotHasChanged;
import com.vomiter.survivorsdelight.common.device.cooking_pot.bridge.ICookingPotTFCRecipeBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

@Mixin(SyncedBlockEntity.class)
public class SyncedBlockEntity_CookingPotMixin implements ICookingPotHasChanged {
    @Unique boolean sdtfc$hasChanged;

    @Override
    public boolean sdtfc$getHasChanged() {
        return sdtfc$hasChanged;
    }

    @Override
    public void sdtfc$setChanged(boolean b) {
        sdtfc$hasChanged = b;
    }

    @Inject(method = "inventoryChanged", at = @At("HEAD"))
    private void setChangedForCookingPot(CallbackInfo ci){
        var self = (SyncedBlockEntity)(Object)this;
        if(self instanceof CookingPotBlockEntity cookingPot){
            sdtfc$setChanged(true);
            ((ICookingPotTFCRecipeBridge)cookingPot).sdtfc$setBridgeCached(null);
        }
    }
}

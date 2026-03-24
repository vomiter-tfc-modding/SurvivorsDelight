package com.vomiter.survivorsdelight.mixin.food.effect;

import com.vomiter.survivorsabilities.core.SAAttributes;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.effect.NourishmentEffect;

import java.util.UUID;

@Mixin(NourishmentEffect.class)
public class NourishmentEffect_Mixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addAttributes(CallbackInfo ci){
        var self = (NourishmentEffect)(Object)this;
        UUID SD_NOURISHMENT_UUID = UUID.fromString("c6c6b2e8-1db3-4a8c-9c63-3d2ba8d7f5c5");
        self.addAttributeModifier(SAAttributes.HUNGER_TOLERANCE, SDUtils.RLUtils.build("nourishment_effect"),10, AttributeModifier.Operation.ADD_VALUE);
    }
}

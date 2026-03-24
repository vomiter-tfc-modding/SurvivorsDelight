package com.vomiter.survivorsdelight.mixin.food.effect;

import com.vomiter.survivorsabilities.core.SAAttributes;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.effect.ComfortEffect;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.UUID;

@Mixin(ComfortEffect.class)
public class ComfortEffect_Mixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addAttributes(CallbackInfo ci){
        var self = (ComfortEffect)(Object)this;
        self.addAttributeModifier(SAAttributes.RESILIENCE, SDUtils.RLUtils.build("comfort_effect"),3, AttributeModifier.Operation.ADD_VALUE);
    }

    @Inject(method = "applyEffectTick", at = @At("HEAD"), cancellable = true)
    private void disableVanilla(LivingEntity entity, int amplifier, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(false);
    }

}

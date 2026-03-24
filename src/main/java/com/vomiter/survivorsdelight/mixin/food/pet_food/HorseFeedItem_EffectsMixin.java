package com.vomiter.survivorsdelight.mixin.food.pet_food;

import com.vomiter.survivorsabilities.core.SAEffects;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.item.HorseFeedItem;

import java.util.List;

@Mixin(HorseFeedItem.class)
public abstract class HorseFeedItem_EffectsMixin {

    @Shadow @Final public static List<MobEffectInstance> EFFECTS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void appendWorkhorseEffect(CallbackInfo ci) {
        EFFECTS.add(new MobEffectInstance(SAEffects.WORKHORSE, 20 * 60 * 5, 1));
    }
}

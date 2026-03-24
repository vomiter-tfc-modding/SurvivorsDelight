package com.vomiter.survivorsdelight.mixin.heat.source.compat.firmalife;

import com.eerussianguy.firmalife.common.blockentities.OvenBottomBlockEntity;
import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = OvenBottomBlockEntity.class, remap = false)
public abstract class Oven_HSMixin implements HeatSourceBlockEntity {
    @Shadow
    public abstract float getTemperature();
    @Override
    public float sdtfc$getTemperature() {
        return getTemperature();
    }
}

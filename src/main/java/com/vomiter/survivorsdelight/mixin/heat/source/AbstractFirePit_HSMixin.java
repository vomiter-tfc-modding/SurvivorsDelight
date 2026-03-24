package com.vomiter.survivorsdelight.mixin.heat.source;

import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = AbstractFirepitBlockEntity.class, remap = false)
public abstract class AbstractFirePit_HSMixin implements HeatSourceBlockEntity {
    @Shadow
    public abstract float getTemperature();
    @Override
    public float sdtfc$getTemperature() {
        return getTemperature();
    }
}

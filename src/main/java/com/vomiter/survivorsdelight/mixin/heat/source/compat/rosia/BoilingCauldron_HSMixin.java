package com.vomiter.survivorsdelight.mixin.heat.source.compat.rosia;

//import com.jewey.rosia.common.blocks.entity.block_entity.BoilingCauldronBlockEntity;
import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//@Mixin(value = BoilingCauldronBlockEntity.class, remap = false)
public abstract class BoilingCauldron_HSMixin implements HeatSourceBlockEntity {
    @Shadow
    public abstract float getTemperature();
    @Override
    public float sdtfc$getTemperature() {
        return getTemperature();
    }
}

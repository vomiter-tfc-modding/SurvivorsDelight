package com.vomiter.survivorsdelight.mixin.heat.source.compat.rosia;

//import com.jewey.rosia.common.blocks.entity.block_entity.LavaBasinBlockEntity;
import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//@Mixin(value = LavaBasinBlockEntity.class, remap = false)
public abstract class LavaBasin_HSMixin implements HeatSourceBlockEntity {
    @Shadow
    public abstract float getTemperature();
    @Override
    public float sdtfc$getTemperature() {
        return getTemperature();
    }
}

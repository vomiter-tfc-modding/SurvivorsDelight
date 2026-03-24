package com.vomiter.survivorsdelight.mixin.farming.farmland;

import com.google.gson.JsonObject;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClimateRange.class)
public interface ClimateRangeInvoker {
    @Invoker("<init>")
    static ClimateRange sdtfc$new(ResourceLocation id, JsonObject json) {
        throw new AssertionError();
    }

    @Accessor("hydrationWiggleRange")
    int getHydrationWiggleRange();

    @Accessor("temperatureWiggleRange")
    float getTemperatureWiggleRange();
}
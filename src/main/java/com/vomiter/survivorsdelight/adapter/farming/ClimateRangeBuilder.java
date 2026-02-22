package com.vomiter.survivorsdelight.adapter.farming;

import com.vomiter.survivorsdelight.SDConfig;
import com.vomiter.survivorsdelight.mixin.farming.farmland.ClimateRangeInvoker;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class ClimateRangeBuilder {
    public static ClimateRange deriveLoose(ResourceLocation baseId) {
        var entry = ClimateRange.MANAGER.get(baseId);
        var src   = entry != null ? entry.get() : ClimateRange.NOOP;

        var j = new com.google.gson.JsonObject();
        int tempExp = SDConfig.RICH_SOIL_FARMLAND_TEMPERATURE_EXPANSION;
        int hydraExp = SDConfig.RICH_SOIL_FARMLAND_HYDRATION_EXPANSION;

        j.addProperty("min_hydration", Math.max(0, src.getMinHydration(false) - hydraExp));
        j.addProperty("max_hydration", Math.min(100, src.getMaxHydration(false) + hydraExp));
        j.addProperty("hydration_wiggle_range", ((ClimateRangeInvoker)src).getHydrationWiggleRange());
        j.addProperty("min_temperature", src.getMinTemperature(false) - tempExp);
        j.addProperty("max_temperature", src.getMaxTemperature(false) + tempExp);
        j.addProperty("temperature_wiggle_range", ((ClimateRangeInvoker)src).getTemperatureWiggleRange());

        return ClimateRangeInvoker.sdtfc$new(
                new ResourceLocation("survivorsdelight", baseId.getNamespace() + "/" + baseId.getPath() + "_loose"),
                j
        );
    }

    public static Supplier<ClimateRange> looseOf(ResourceLocation baseId) {
        return () -> deriveLoose(baseId);
    }
}

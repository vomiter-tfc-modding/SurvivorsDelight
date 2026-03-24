package com.vomiter.survivorsdelight.common.farming;

import com.vomiter.survivorsdelight.SDConfig;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.resources.ResourceLocation;

public class ClimateRangeBuilder {
    public static ClimateRange deriveLoose(ClimateRange original) {
        var src   = original != null ? original : ClimateRange.NOOP;
        int tempExp = SDConfig.RICH_SOIL_FARMLAND_TEMPERATURE_EXPANSION;
        int hydraExp = SDConfig.RICH_SOIL_FARMLAND_HYDRATION_EXPANSION;
        return new ClimateRange(
                Math.max(0, src.getMinHydration(false) - hydraExp),
                Math.min(100, src.getMaxHydration(false) + hydraExp),
                src.hydrationWiggleRange(),
                src.getMinTemperature(false) - tempExp,
                src.getMaxTemperature(false) + tempExp,
                src.temperatureWiggleRange()
        );
    }

    public static ClimateRange deriveLoose(ResourceLocation baseId) {
        var entry = ClimateRange.MANAGER.get(baseId);
        return deriveLoose(entry);
    }
}

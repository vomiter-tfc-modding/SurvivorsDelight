package com.vomiter.survivorsdelight;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = SurvivorsDelight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SDConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    /*
     * Runtime cached values (static variables)
     * Use these fields in gameplay code instead of calling ForgeConfigSpec.Value#get() repeatedly.
     */
    public static int SKILLET_SLOT_NUMBER;
    public static int RICH_SOIL_GROWTH_BOOST_TICK;
    public static int RICH_SOIL_FARMLAND_TEMPERATURE_EXPANSION;
    public static int RICH_SOIL_FARMLAND_HYDRATION_EXPANSION;
    public static double TRAIT_CABINET_STORED_MODIFIER;
    public static double TRAIT_SKILLET_COOKED_MODIFIER;
    public static boolean RICH_SOIL_FARMLAND_ALLOW_NON_TFC_CROP;
    public static double RICH_SOIL_RANDOM_MUSHROOM_CHANCE;
    public static double RICH_SOIL_RANDOM_MUSHROOM_BROWN_CHANCE;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_SPEC);
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue skilletSlotNumber;
        public final ForgeConfigSpec.IntValue richSoilGrowthBoostTick;
        public final ForgeConfigSpec.IntValue richSoilFarmlandTemperatureExpansion;
        public final ForgeConfigSpec.IntValue richSoilFarmlandHydrationExpansion;
        public final ForgeConfigSpec.DoubleValue traitCabinetStoredModifier;
        public final ForgeConfigSpec.DoubleValue traitSkilletCookedModifier;
        public final ForgeConfigSpec.BooleanValue richSoilFarmlandAllowNonTFCCrop;
        public final ForgeConfigSpec.DoubleValue richSoilRandomMushroomChance;
        public final ForgeConfigSpec.DoubleValue richSoilRandomMushroomBrownChance;

        public Common(ForgeConfigSpec.Builder builder) {
            builder.push("general");

            richSoilFarmlandAllowNonTFCCrop = builder
                    .comment("If false, crops with no proper crop block entity would be popped off on rich soil farmlands.")
                    .define("richSoilFarmlandAllowNonTFCCrop", true);

            skilletSlotNumber = builder
                    .comment("How many items can be put into skillet block at once.")
                    .defineInRange("skilletSlotNumber", 8, 1, 32);

            traitCabinetStoredModifier = builder
                    .comment("The modifier for the 'Cabinet Stored' food trait. Values less than 1 extend food lifetime, values greater than one decrease it. A value of zero stops decay.")
                    .defineInRange("traitCabinetStoredModifier", 0.5, 0f, Double.MAX_VALUE);

            traitSkilletCookedModifier = builder
                    .comment("The modifier for the 'Skillet Cooked' food trait. Values less than 1 extend food lifetime, values greater than one decrease it. A value of zero stops decay.")
                    .defineInRange("traitSkilletCookedModifier", 0.8, 0.0F, Double.MAX_VALUE);

            richSoilGrowthBoostTick = builder
                    .comment("How many ticks rich soil should boost the growth of the block above it.")
                    .defineInRange("richSoilGrowthBoostTick", 2400, 0, 24000 * 10);

            richSoilFarmlandTemperatureExpansion = builder
                    .comment("How many degrees of temperature deviated from usual range is allowed for crops planted on rich soil farmlands to grow.")
                    .defineInRange("richSoilFarmlandTemperatureExpansion", 5, 0, 100);

            richSoilFarmlandHydrationExpansion = builder
                    .comment("How many percentile of hydration deviated from usual range is allowed for crops planted on rich soil farmlands to grow.")
                    .defineInRange("richSoilFarmlandHydrationExpansion", 5, 0, 100);

            richSoilRandomMushroomChance = builder
                    .comment("Chance (0~1) for rich soil random tick to spawn a mushroom on the block above, if it is air.")
                    .defineInRange("richSoilRandomMushroomChance", 1.0D, 0.0D, 1.0D);

            richSoilRandomMushroomBrownChance = builder
                    .comment("Chance (0~1) that the spawned mushroom is a brown mushroom. (Otherwise red mushroom)")
                    .defineInRange("richSoilRandomMushroomBrownChance", 0.1D, 0.0D, 1.0D);

            builder.pop();
        }
    }

    private static void bakeCommon() {
        // Pull values from spec and store into static fields
        RICH_SOIL_FARMLAND_ALLOW_NON_TFC_CROP = COMMON.richSoilFarmlandAllowNonTFCCrop.get();

        SKILLET_SLOT_NUMBER = COMMON.skilletSlotNumber.get();
        TRAIT_CABINET_STORED_MODIFIER = COMMON.traitCabinetStoredModifier.get();
        TRAIT_SKILLET_COOKED_MODIFIER = COMMON.traitSkilletCookedModifier.get();

        RICH_SOIL_GROWTH_BOOST_TICK = COMMON.richSoilGrowthBoostTick.get();
        RICH_SOIL_FARMLAND_TEMPERATURE_EXPANSION = COMMON.richSoilFarmlandTemperatureExpansion.get();
        RICH_SOIL_FARMLAND_HYDRATION_EXPANSION = COMMON.richSoilFarmlandHydrationExpansion.get();
        RICH_SOIL_RANDOM_MUSHROOM_CHANCE = COMMON.richSoilRandomMushroomChance.get();
        RICH_SOIL_RANDOM_MUSHROOM_BROWN_CHANCE = COMMON.richSoilRandomMushroomBrownChance.get();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            bakeCommon();
            SurvivorsDelight.LOGGER.info("SurvivorsDelight Config Loaded: {}", configEvent.getConfig().getFileName());
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading configEvent) {
        if (configEvent.getConfig().getSpec() == COMMON_SPEC) {
            bakeCommon();
            SurvivorsDelight.LOGGER.info("SurvivorsDelight Config Reloaded: {}", configEvent.getConfig().getFileName());
        }
    }
}
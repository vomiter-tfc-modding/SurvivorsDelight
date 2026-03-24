package com.vomiter.survivorsdelight;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SurvivorsDelight.MODID)
public class SDConfig {

    // =======================
    // Spec & 定義
    // =======================

    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    // =======================
    // 快取欄位（給 runtime 用）
    // =======================

    // Skillet 一次能放幾個
    public static int SKILLET_SLOT_LIMIT = 8; // default

    // Rich Soil 成長加速 tick 數
    public static int RICH_SOIL_GROWTH_BOOST_TICK = 2400; // default

    // Rich Soil Farmland 溫度容許擴張（度數）
    public static int RICH_SOIL_FARMLAND_TEMPERATURE_EXPANSION = 5; // default

    // Rich Soil Farmland 水分容許擴張（百分比）
    public static int RICH_SOIL_FARMLAND_HYDRATION_EXPANSION = 5; // default

    // 櫃子保存食物 trait 修正值
    public static double TRAIT_CABINET_STORED_MODIFIER = 0.5D; // default

    // Skillet 煎烤食物 trait 修正值
    public static double TRAIT_SKILLET_COOKED_MODIFIER = 0.8D; // default

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        COMMON_SPEC = builder.build();
    }

    public static class Common {

        public final ModConfigSpec.IntValue skilletSlotNumber;
        public final ModConfigSpec.IntValue richSoilGrowthBoostTick;
        public final ModConfigSpec.IntValue richSoilFarmlandTemperatureExpansion;
        public final ModConfigSpec.IntValue richSoilFarmlandHydrationExpansion;
        public final ModConfigSpec.DoubleValue traitCabinetStoredModifier;
        public final ModConfigSpec.DoubleValue traitSkilletCookedModifier;

        public Common(ModConfigSpec.Builder builder) {
            builder.push("general");

            skilletSlotNumber = builder
                    .comment("How many items can be put into skillet block at once.")
                    .defineInRange("skilletSlotNumber", 8, 1, 32);

            traitCabinetStoredModifier = builder
                    .comment("The modifier for the 'Cabinet Stored' food trait. Values less than 1 extend food lifetime, values greater than one decrease it. A value of zero stops decay.")
                    .defineInRange("traitCabinetStoredModifier", 0.5, 0.0, Double.MAX_VALUE);

            traitSkilletCookedModifier = builder
                    .comment("The modifier for the 'Skillet Cooked' food trait. Values less than 1 extend food lifetime, values greater than one decrease it. A value of zero stops decay.")
                    .defineInRange("traitSkilletCookedModifier", 0.8, 0.0, Double.MAX_VALUE);

            richSoilGrowthBoostTick = builder
                    .comment("How many ticks rich soil should boost the growth of the block above it.")
                    .defineInRange("richSoilGrowthBoostTick", 2400, 0, 24000 * 10);

            richSoilFarmlandTemperatureExpansion = builder
                    .comment("How many degrees of temperature deviated from usual range is allowed for crops planted on rich soil farmlands to grow.")
                    .defineInRange("richSoilFarmlandTemperatureExpansion", 5, 0, 100);

            richSoilFarmlandHydrationExpansion = builder
                    .comment("How many percentile of hydration deviated from usual range is allowed for crops planted on rich soil farmlands to grow.")
                    .defineInRange("richSoilFarmlandHydrationExpansion", 5, 0, 100);

            builder.pop();
        }
    }

    // =======================
    // 載入 / 重載事件：同步快取
    // =======================

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            SurvivorsDelight.LOGGER.info("SurvivorsDelight Config Loaded: {}", event.getConfig().getFileName());
            syncCachedValues();
        }
    }

    @SubscribeEvent
    public static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == COMMON_SPEC) {
            SurvivorsDelight.LOGGER.info("SurvivorsDelight Config Reloaded: {}", event.getConfig().getFileName());
            syncCachedValues();
        }
    }

    private static void syncCachedValues() {
        // 保護一下：COMMON 會在 static block 初始化，這裡正常不會是 null
        if (COMMON == null) return;

        SKILLET_SLOT_LIMIT = COMMON.skilletSlotNumber.get();
        TRAIT_CABINET_STORED_MODIFIER = COMMON.traitCabinetStoredModifier.get();
        TRAIT_SKILLET_COOKED_MODIFIER = COMMON.traitSkilletCookedModifier.get();
        RICH_SOIL_GROWTH_BOOST_TICK = COMMON.richSoilGrowthBoostTick.get();
        RICH_SOIL_FARMLAND_TEMPERATURE_EXPANSION = COMMON.richSoilFarmlandTemperatureExpansion.get();
        RICH_SOIL_FARMLAND_HYDRATION_EXPANSION = COMMON.richSoilFarmlandHydrationExpansion.get();
    }
}

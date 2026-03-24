package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CookingPotExtraNutrientRules {
    private static final List<Entry> RULES = new ArrayList<>();
    private static boolean bootstrapped = false;

    private CookingPotExtraNutrientRules() {
    }

    public static float getExtraNutrient(Level level, ItemStack stack, Nutrient nutrient, FoodData data) {
        ensureBootstrapped();

        for (Entry entry : RULES) {
            CookingPotExtraNutrientRule rule = entry.rule();
            if (rule.matches(level, stack, nutrient, data)) {
                return rule.getValue(level, stack, nutrient, data);
            }
        }
        return 0f;
    }

    public static synchronized void register(ResourceLocation id, int priority, CookingPotExtraNutrientRule rule) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(rule, "rule");

        for (Entry entry : RULES) {
            if (entry.id().equals(id)) {
                throw new IllegalArgumentException("Duplicate cooking pot extra nutrient rule id: " + id);
            }
        }

        RULES.add(new Entry(id, priority, rule));
        RULES.sort(ENTRY_COMPARATOR);
    }

    public static synchronized boolean isRegistered(ResourceLocation id) {
        for (Entry entry : RULES) {
            if (entry.id().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static synchronized List<ResourceLocation> getRegisteredRuleIds() {
        List<ResourceLocation> ids = new ArrayList<>(RULES.size());
        for (Entry entry : RULES) {
            ids.add(entry.id());
        }
        return List.copyOf(ids);
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;

        register(
                SDUtils.RLUtils.build("survivorsdelight", "raw_pasta_grain_bonus"),
                1000,
                new SimpleRule(
                        (level, stack, nutrient, data) ->
                                nutrient == Nutrient.GRAIN && stack.is(ModItems.RAW_PASTA.get()),
                        (level, stack, nutrient, data) -> 1.0f
                )
        );

        register(
                SDUtils.RLUtils.build("survivorsdelight", "tfc_grain_tag_bonus"),
                900,
                new SimpleRule(
                        (level, stack, nutrient, data) ->
                                nutrient == Nutrient.GRAIN && stack.is(SDTags.ItemTags.create("tfc", "foods/grains")),
                        (level, stack, nutrient, data) -> 1.0f
                )
        );

        register(
                SDUtils.RLUtils.build("survivorsdelight", "firmalife_extra_dough_bonus"),
                800,
                new SimpleRule(
                        (level, stack, nutrient, data) ->
                                nutrient == Nutrient.GRAIN && stack.is(SDTags.ItemTags.create("firmalife", "foods/extra_dough")),
                        (level, stack, nutrient, data) -> 1.5f
                )
        );

        register(
                SDUtils.RLUtils.build("survivorsdelight", "tfc_dough_cooked_bonus"),
                700,
                new SimpleRule(
                        (level, stack, nutrient, data) ->
                                nutrient == Nutrient.GRAIN && stack.is(SDTags.ItemTags.TFC_DOUGHS),
                        (level, stack, nutrient, data) ->
                                SDUtils.getExtraNutrientAfterCooking(stack, Nutrient.GRAIN, level)
                                        + data.nutrient(nutrient) * 0.2f
                )
        );

        register(
                SDUtils.RLUtils.build("survivorsdelight", "tfc_raw_meat_cooked_bonus"),
                600,
                new SimpleRule(
                        (level, stack, nutrient, data) ->
                                nutrient == Nutrient.PROTEIN && stack.is(SDTags.ItemTags.TFC_RAW_MEATS),
                        (level, stack, nutrient, data) ->
                                SDUtils.getExtraNutrientAfterCooking(stack, Nutrient.PROTEIN, level)
                                        + data.nutrient(nutrient) * 0.2f
                )
        );
    }

    private static void ensureBootstrapped() {
        if (!bootstrapped) {
            bootstrap();
        }
    }

    private static final Comparator<Entry> ENTRY_COMPARATOR =
            Comparator.comparingInt(Entry::priority).reversed()
                    .thenComparing(entry -> entry.id().toString());

    @FunctionalInterface
    public interface RuleMatcher {
        boolean matches(Level level, ItemStack stack, Nutrient nutrient, FoodData data);
    }

    @FunctionalInterface
    public interface RuleValueProvider {
        float getValue(Level level, ItemStack stack, Nutrient nutrient, FoodData data);
    }

    public static final class SimpleRule implements CookingPotExtraNutrientRule {
        private final RuleMatcher matcher;
        private final RuleValueProvider valueProvider;

        public SimpleRule(RuleMatcher matcher, RuleValueProvider valueProvider) {
            this.matcher = Objects.requireNonNull(matcher, "matcher");
            this.valueProvider = Objects.requireNonNull(valueProvider, "valueProvider");
        }

        @Override
        public boolean matches(Level level, ItemStack stack, Nutrient nutrient, FoodData data) {
            return matcher.matches(level, stack, nutrient, data);
        }

        @Override
        public float getValue(Level level, ItemStack stack, Nutrient nutrient, FoodData data) {
            return valueProvider.getValue(level, stack, nutrient, data);
        }
    }

    private record Entry(ResourceLocation id, int priority, CookingPotExtraNutrientRule rule) {
    }
}
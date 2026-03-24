package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.SDConfig;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class CookingPotContributionModifiers {
    private static final List<Entry> RULES = new ArrayList<>();
    private static boolean bootstrapped = false;

    private CookingPotContributionModifiers() {
    }

    public static float apply(
            Level level,
            ItemStack stack,
            Nutrient nutrient,
            FoodData data,
            float current,
            CookingPotNutritionContext context
    ) {
        ensureBootstrapped();

        float value = current;
        for (Entry entry : RULES) {
            CookingPotContributionModifier rule = entry.rule();
            if (rule.matches(level, stack, nutrient, data, context)) {
                value = rule.modify(level, stack, nutrient, data, value, context);
            }
        }
        return value;
    }

    public static synchronized void register(ResourceLocation id, int priority, CookingPotContributionModifier rule) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(rule, "rule");

        for (Entry entry : RULES) {
            if (entry.id().equals(id)) {
                throw new IllegalArgumentException("Duplicate cooking pot contribution modifier id: " + id);
            }
        }

        RULES.add(new Entry(id, priority, rule));
        RULES.sort(ENTRY_COMPARATOR);
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) {
            return;
        }
        bootstrapped = true;

        if(SDConfig.REBALANCING_FEAST){
            register(
                    id("sweetener_bonus"),
                    1000,
                    new SimpleModifier(
                            (level, stack, nutrient, data, context) ->
                                    context.contains(SDTags.ItemTags.TFC_SWEETENER)
                                            && !stack.is(SDTags.ItemTags.TFC_SWEETENER),
                            (level, stack, nutrient, data, current, context) ->
                            {
                                if(nutrient == Nutrient.VEGETABLES || nutrient == Nutrient.FRUIT) return current * 2;
                                return current;
                            }
                    )
            );


            register(
                    id("salt_bonus"),
                    900,
                    new SimpleModifier(
                            (level, stack, nutrient, data, context) ->
                                    context.contains(TFCItems.POWDERS.get(Powder.SALT).get()),
                            (level, stack, nutrient, data, current, context) ->
                                    nutrient == Nutrient.PROTEIN ? current * 1.2f : current
                    )
            );
            register(
                    id("oil_bonus"),
                    800,
                    new SimpleModifier(
                            (level, stack, nutrient, data, context) ->
                                    context.fluidIs(SDTags.FluidTags.COOKING_OILS),
                            (level, stack, nutrient, data, current, context) -> {
                                if(context.outputPreview().is(SDTags.ItemTags.FEAST_BLOCKS)){
                                    if(nutrient == Nutrient.PROTEIN) return current * 1.5f;
                                    if(nutrient == Nutrient.VEGETABLES) return current * 2;
                                    if(nutrient == Nutrient.FRUIT) return current * 2;
                                    if(nutrient == Nutrient.GRAIN) return current * 2;
                                }
                                else {
                                    if(nutrient == Nutrient.PROTEIN) return current * 1.1f;
                                    if(nutrient == Nutrient.VEGETABLES) return current * 1.3f;
                                    if(nutrient == Nutrient.FRUIT) return current * 1.3f;
                                    if(nutrient == Nutrient.GRAIN) return current * 1.3f;
                                }
                                return current;
                            }
                    )
            );
            register(
                    id("feast_bonus"),
                    0,
                    new SimpleModifier(
                            (level, stack, nutrient, data, context) -> context.outputPreview().is(SDTags.ItemTags.FEAST_BLOCKS),
                            (level, stack, nutrient, data, current, context) ->{
                                if(current == 0) return 0;
                                return (float) Math.max(current, 1.6);
                            }
                    )
            );
        }

        register(
                id("milk_bonus"),
                800,
                new SimpleModifier(
                        (level, stack, nutrient, data, context) ->
                                context.fluidIs(SDTags.FluidTags.TFC_MILKS),
                        (level, stack, nutrient, data, current, context) ->
                                nutrient == Nutrient.DAIRY ? current + 1f / context.foodIngredientCount() : current
                )
        );


        /*
        register(
                id("spice_bonus"),
                800,
                new SimpleModifier(
                        (level, stack, nutrient, data, context) ->
                                context.contains(SDTags.ItemTags.SPICES),
                        (level, stack, nutrient, data, current, context) ->
                                (nutrient == Nutrient.FRUIT || nutrient == Nutrient.VEGETABLE)
                                        ? current + 0.25f
                                        : current
                )
        );
        */
    }

    private static void ensureBootstrapped() {
        if (!bootstrapped) {
            bootstrap();
        }
    }

    private static ResourceLocation id(String path) {
        return SDUtils.RLUtils.build("survivorsdelight", path);
    }

    private static final Comparator<Entry> ENTRY_COMPARATOR =
            Comparator.comparingInt(Entry::priority).reversed()
                    .thenComparing(entry -> entry.id().toString());

    @FunctionalInterface
    public interface ModifierMatcher {
        boolean matches(
                Level level,
                ItemStack stack,
                Nutrient nutrient,
                FoodData data,
                CookingPotNutritionContext context
        );
    }

    @FunctionalInterface
    public interface ModifierOperator {
        float modify(
                Level level,
                ItemStack stack,
                Nutrient nutrient,
                FoodData data,
                float current,
                CookingPotNutritionContext context
        );
    }

    public static final class SimpleModifier implements CookingPotContributionModifier {
        private final ModifierMatcher matcher;
        private final ModifierOperator operator;

        public SimpleModifier(ModifierMatcher matcher, ModifierOperator operator) {
            this.matcher = Objects.requireNonNull(matcher, "matcher");
            this.operator = Objects.requireNonNull(operator, "operator");
        }

        @Override
        public boolean matches(
                Level level,
                ItemStack stack,
                Nutrient nutrient,
                FoodData data,
                CookingPotNutritionContext context
        ) {
            return matcher.matches(level, stack, nutrient, data, context);
        }

        @Override
        public float modify(
                Level level,
                ItemStack stack,
                Nutrient nutrient,
                FoodData data,
                float current,
                CookingPotNutritionContext context
        ) {
            return operator.modify(level, stack, nutrient, data, current, context);
        }
    }

    private record Entry(ResourceLocation id, int priority, CookingPotContributionModifier rule) {
    }
}
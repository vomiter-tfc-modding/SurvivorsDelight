package com.vomiter.survivorsdelight.api.cooking;

import com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic.DynamicFoodContext;
import com.vomiter.survivorsdelight.util.FoodDataBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extension point for cooking-pot nutrition. Register rules from common setup's
 * {@code enqueueWork}; registration closes during SD's load-complete work.
 *
 * <p>Higher priorities run first. Equal priorities retain registration order.
 * Each rule visits every input slot (including empty slots), then the TOTAL
 * context, before the next rule runs. All callbacks share the accumulated food
 * builder. A callback may mutate it or return a replacement, but never null.
 *
 * <p>The pot applies its balance multiplier, hunger and decay settings after
 * these rules. Context items and fluids are read-only inputs to callbacks;
 * callbacks must not consume items, drain tanks or otherwise change the world.
 */
public final class DynamicCookingRules {
    public static final int PRIORITY_ADDITION = 2000;
    public static final int PRIORITY_MULTIPLICATION = 1000;
    public static final int PRIORITY_FLUID = 0;

    private static final Map<ResourceLocation, RuleHolder> RULES = new LinkedHashMap<>();
    private static volatile List<RuleHolder> orderedRules;

    private DynamicCookingRules() {}

    @FunctionalInterface
    public interface PotFoodModifier {
        FoodDataBuilder modify(FoodDataBuilder food, DynamicFoodContext<CookingPotRecipe> context);
    }

    public record RuleHolder(int priority, ResourceLocation id, PotFoodModifier modifier) {
        public RuleHolder {
            Objects.requireNonNull(id, "Rule ID");
            Objects.requireNonNull(modifier, "Rule modifier");
        }
    }

    /**
     * Registers a unique rule. Use your addon's namespace for the ID.
     *
     * @throws IllegalArgumentException if the ID is already registered
     * @throws IllegalStateException if registration has closed
     */
    public static synchronized void register(RuleHolder rule) {
        Objects.requireNonNull(rule, "Rule");
        if (orderedRules != null) {
            throw new IllegalStateException("Dynamic cooking rule registration is closed: " + rule.id()
                    + ". Register during FMLCommonSetupEvent.enqueueWork.");
        }
        if (RULES.putIfAbsent(rule.id(), rule) != null) {
            throw new IllegalArgumentException("Duplicate dynamic cooking rule ID: " + rule.id());
        }
    }

    public static void register(ResourceLocation id, int priority, PotFoodModifier modifier) {
        register(new RuleHolder(priority, id, modifier));
    }

    /** Called by SD after common setup has finished for all mods. */
    @ApiStatus.Internal
    public static synchronized void freeze() {
        if (orderedRules == null) {
            orderedRules = RULES.values().stream()
                    .sorted(Comparator.comparingInt(RuleHolder::priority).reversed())
                    .toList();
        }
    }

    /** Returns the immutable execution order after registration has closed. */
    public static List<RuleHolder> getRules() {
        List<RuleHolder> rules = orderedRules;
        if (rules == null) {
            throw new IllegalStateException("Dynamic cooking rules are not ready until load complete.");
        }
        return rules;
    }

    /**
     * Evaluates the registered rules without applying the pot's final balancing.
     * The supplied TOTAL context contains the output stack and the inputs for
     * one batch. The returned builder is authoritative, even when it differs
     * from the supplied builder.
     */
    public static FoodDataBuilder apply(
            FoodDataBuilder food, DynamicFoodContext<CookingPotRecipe> totalContext) {
        Objects.requireNonNull(food, "Food builder");
        Objects.requireNonNull(totalContext, "Total context");
        if (totalContext.phase() != DynamicFoodContext.Phase.TOTAL) {
            throw new IllegalArgumentException("DynamicCookingRules.apply requires a TOTAL context.");
        }
        for (RuleHolder rule : getRules()) {
            for (ItemStack input : totalContext.inputs()) {
                var context = new DynamicFoodContext<>(
                        totalContext.inputs(), totalContext.inputFluids(), input,
                        totalContext.recipe(), totalContext.recipeId(),
                        DynamicFoodContext.Phase.INDIVIDUAL, totalContext.level());
                food = applyRule(rule, food, context);
            }
            food = applyRule(rule, food, totalContext);
        }
        return food;
    }

    private static FoodDataBuilder applyRule(
            RuleHolder rule, FoodDataBuilder food, DynamicFoodContext<CookingPotRecipe> context) {
        return Objects.requireNonNull(rule.modifier().modify(food, context),
                "Dynamic cooking rule " + rule.id() + " returned null in " + context.phase());
    }
}

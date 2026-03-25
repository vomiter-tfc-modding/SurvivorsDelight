package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.CookingPotExtraNutrientRules;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.component.item.ItemListComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface ICookingPotCalcDynamic {
    ItemStack sdtfc$getCachedDynamic();
    void sdtfc$setCachedDynamic(ItemStack stack);

    default ItemStack calcDynamicResult(RecipeWrapper wrapper, SDCookingPotRecipe recipe, Level level) {
        return calcDynamicResult(wrapper, recipe, level, null);
    }

    default ItemStack calcDynamicResult(
            RecipeWrapper wrapper,
            SDCookingPotRecipe recipe,
            Level level,
            @Nullable ResourceLocation recipeId
    ) {
        final ItemStack result = recipe.getResultStack().copy();
        final int resultCount = Math.max(1, result.getCount());

        final List<ItemStack> inputStacks = collectInputStacks(wrapper);
        final int foodIngredientCount = countFoodIngredients(inputStacks);
        @Nullable Fluid fluid;
        if(this instanceof ICookingPotFluidAccess fluidAccess){
            fluid = Objects.requireNonNull(fluidAccess.sd$getFluidHandler()).getFluidInTank(0).getFluid();
        }
        else fluid = null;

        final CookingPotNutritionContext ctx = CookingPotNutritionContext.of(
                inputStacks,
                foodIngredientCount,
                result.copy(),
                recipe,
                recipeId,
                fluid
        );

        final DynamicPotFoodAccumulator acc = accumulate(ctx, level, resultCount);

        final List<ItemStack> sortedIngredients = new ArrayList<>(acc.foodIngredients());
        sortedIngredients.sort(
                Comparator.comparing(ItemStack::getCount)
                        .thenComparing(stack -> Objects.requireNonNull(stack.getItemHolder().getKey()).location())
        );

        result.set(TFCComponents.INGREDIENTS, ItemListComponent.of(sortedIngredients));
        FoodCapability.setFoodForDynamicItemOnCreate(
                result,
                new FoodData(
                        acc.hunger(),
                        acc.water(),
                        acc.saturation(),
                        0,
                        acc.nutrition(),
                        4.5f
                )
        );
        return result;
    }

    private static List<ItemStack> collectInputStacks(RecipeWrapper wrapper) {
        final List<ItemStack> stacks = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            final ItemStack stack = wrapper.getItem(i);
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }
        return stacks;
    }

    private static int countFoodIngredients(List<ItemStack> inputStacks) {
        int count = 0;
        for (ItemStack stack : inputStacks) {
            if (FoodCapability.get(stack) != null) {
                count++;
            }
        }
        return count;
    }

    private static DynamicPotFoodAccumulator accumulate(
            CookingPotNutritionContext ctx,
            Level level,
            int resultCount
    ) {
        final FoodData baseFood = FoodData.of(4.5f);
        final float[] nutrition = baseFood.nutrients().clone();
        float saturation = baseFood.saturation();
        float water = baseFood.water();
        int hunger = 0;

        final List<ItemStack> foodIngredients = new ArrayList<>();

        for (ItemStack stack : ctx.inputStacks()) {
            final var cap = FoodCapability.get(stack);
            if (cap == null) {
                continue;
            }

            final FoodData data = cap.getData();
            foodIngredients.add(stack.copyWithCount(1));

            for (Nutrient nutrient : Nutrient.VALUES) {
                float value = data.nutrient(nutrient);
                value += CookingPotExtraNutrientRules.getExtraNutrient(level, stack, nutrient, data);
                value = CookingPotContributionModifiers.apply(
                        level,
                        stack,
                        nutrient,
                        data,
                        value,
                        ctx
                );

                nutrition[nutrient.ordinal()] += value / resultCount;
            }

            water += data.water() / resultCount;
            saturation += data.saturation() / resultCount;
            hunger = Math.max(hunger, data.hunger());
        }

        return new DynamicPotFoodAccumulator(
                nutrition,
                saturation,
                water,
                hunger,
                foodIngredients
        );
    }

    record DynamicPotFoodAccumulator(
            float[] nutrition,
            float saturation,
            float water,
            int hunger,
            List<ItemStack> foodIngredients
    ) {}
}
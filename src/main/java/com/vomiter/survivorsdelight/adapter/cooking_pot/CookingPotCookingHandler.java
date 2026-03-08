package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.ICookingPotRecipeBridge;
import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.TFCPotRecipeBridgeFD;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CookingPotCookingHandler {
    private static final int INPUT_SLOT_COUNT = 6;
    private static final float DEFAULT_DECAY = 4.5f;
    private static final TagKey<Fluid> MILKS_TAG =
            TagKey.create(Registries.FLUID, SDUtils.RLUtils.build("tfc", "milks"));

    private CookingPotCookingHandler() {
    }

    public static void handleDynamicCookingPotRecipe(
            Level level,
            BlockPos pos,
            BlockState state,
            CookingPotBlockEntity cookingPot,
            CallbackInfo ci,
            Optional<CookingPotRecipe> recipe
    ) {
        ICookingPotRecipeBridge bridge = (ICookingPotRecipeBridge) cookingPot;
        if (!bridge.sdtfc$getCachedDynamicFoodResult().isEmpty()) {
            return;
        }

        recipe.ifPresent(r -> handleRecipe(level, cookingPot, bridge, r));
    }

    private static void handleRecipe(
            Level level,
            CookingPotBlockEntity cookingPot,
            ICookingPotRecipeBridge bridge,
            CookingPotRecipe recipe
    ) {
        if (recipe instanceof TFCPotRecipeBridgeFD) {
            return;
        }

        ItemStack originalResult = recipe.getResultItem(level.registryAccess()).copy();
        if (!(FoodCapability.get(originalResult) instanceof FoodHandler.Dynamic dynamicFood)) {
            return;
        }

        List<ItemStack> inputStacks = collectInputStacks(cookingPot);
        int foodIngredientCount = countFoodIngredients(inputStacks);
        int resultCount = Math.max(1, originalResult.getCount());

        DynamicFoodComputation computation =
                computeDynamicFood(level, recipe, inputStacks, foodIngredientCount, resultCount);

        computation.ingredients().sort(
                Comparator.comparing(ItemStack::getCount)
                        .thenComparing(stack -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())))
        );

        dynamicFood.setIngredients(computation.ingredients());
        dynamicFood.setFood(FoodData.create(
                (5 + computation.hunger()) / 2,
                computation.water(),
                computation.saturation(),
                computation.nutrition(),
                DEFAULT_DECAY
        ));

        bridge.sdtfc$setCachedDynamicFoodResult(originalResult);
    }

    private static List<ItemStack> collectInputStacks(CookingPotBlockEntity cookingPot) {
        List<ItemStack> stacks = new ArrayList<>(INPUT_SLOT_COUNT);
        for (int i = 0; i < INPUT_SLOT_COUNT; i++) {
            ItemStack stack = cookingPot.getInventory().getStackInSlot(i);
            if (!stack.isEmpty()) {
                stacks.add(stack);
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

    private static DynamicFoodComputation computeDynamicFood(
            Level level,
            CookingPotRecipe recipe,
            List<ItemStack> inputStacks,
            int foodIngredientCount,
            int resultCount
    ) {
        FoodData baseFood = FoodData.decayOnly(DEFAULT_DECAY);
        float[] nutrition = baseFood.nutrients();
        float saturation = baseFood.saturation();
        float water = baseFood.water();
        int hunger = 0;
        List<ItemStack> ingredients = new ArrayList<>();

        addFluidNutritionIfNeeded(recipe, nutrition);

        for (ItemStack stack : inputStacks) {
            IFood handler = FoodCapability.get(stack);
            if (handler == null) {
                continue;
            }

            FoodData data = handler.getData();
            ingredients.add(stack.getItem().getDefaultInstance());

            for (Nutrient nutrient : Nutrient.VALUES) {
                float retained = data.nutrient(nutrient) * (1f - 0.04f * foodIngredientCount);
                float extra = CookingPotExtraNutrientRules.getExtraNutrient(level, stack, nutrient, data);
                nutrition[nutrient.ordinal()] += (retained + extra) / resultCount;
            }

            water += data.water() / resultCount;
            saturation += data.saturation() / resultCount;
            hunger = Math.max(hunger, data.hunger());
        }

        return new DynamicFoodComputation(ingredients, nutrition, saturation, water, hunger);
    }

    private static void addFluidNutritionIfNeeded(CookingPotRecipe recipe, float[] nutrition) {
        if (!(recipe instanceof IFluidRequiringRecipe fluidRecipe)) {
            return;
        }

        if (SDUtils.TagUtils.fluidIngredientMatchesTag(fluidRecipe.sdtfc$getFluidIngredient(), MILKS_TAG)) {
            nutrition[Nutrient.DAIRY.ordinal()] += 1.0f;
        }
    }

    private record DynamicFoodComputation(
            List<ItemStack> ingredients,
            float[] nutrition,
            float saturation,
            float water,
            int hunger
    ) {
    }
}
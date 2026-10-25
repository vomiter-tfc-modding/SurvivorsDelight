package com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.api.cooking.DynamicCookingRules;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import com.vomiter.survivorsdelight.util.FoodDataBuilder;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.component.item.ItemListComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CookingPotCookingHandler {
    public static ItemStack calculateDynamicOutput
            (
                    ItemStackHandler inventory,
                    FluidStack fluidStack,
                    Level level,
                    CookingPotRecipe recipe,
                    int slotNumber
            ){
        var resultItem = recipe.getResultItem(level.registryAccess()).copy();
        var resultFood = FoodCapability.get(resultItem);
        if (resultFood == null) return resultItem;
        if (!(recipe instanceof SDCookingPotRecipe cookingPotRecipe)) return resultItem;
        List<ItemStack> inputItems = new ArrayList<>();
        for (int i = 0; i < slotNumber; i++) {
            inputItems.add(inventory.getStackInSlot(i).copyWithCount(1));
        }
        var inputFluid = fluidStack.copy();
        if (!inputFluid.isEmpty()){
            inputFluid.setAmount(cookingPotRecipe.getFluidAmountMb());
        }
        var recipeId = level.getRecipeManager().getRecipes().stream()
                .filter(holder -> holder.value() == recipe)
                .map(RecipeHolder::id).findFirst().orElse(null);
        var context = new DynamicFoodContext<>(
                inputItems, inputFluid, resultItem, recipe, recipeId,
                DynamicFoodContext.Phase.TOTAL, level);
        var foodBuilder = DynamicCookingRules.apply(FoodDataBuilder.from(resultFood.getData()), context);
        SurvivorsDelight.LOGGER.info("Cached Nutrients = {}", foodBuilder.nutrients());

        var inputFood = new ArrayList<>(inputItems.stream().filter(item -> FoodCapability.get(item) != null).toList());
        int hunger = 0;
        for (ItemStack itemStack : inputFood) {
            hunger = Math.max(Objects.requireNonNull(FoodCapability.get(itemStack)).getData().hunger(), hunger);
        }
        var balanceMultiplier = 1f - cookingPotRecipe.getBalanceFactor() * inputFood.size();
        foodBuilder.mulNutrient(balanceMultiplier, Nutrient.GRAIN, Nutrient.VEGETABLES, Nutrient.PROTEIN, Nutrient.FRUIT, Nutrient.DAIRY);
        foodBuilder.decayModifier(4.5f);
        foodBuilder.hunger((hunger + 5) / 2);
        FoodCapability.setFoodForDynamicItemOnCreate(
                resultItem,
                foodBuilder.build()
        );
        inputFood.sort(
                Comparator.comparing(ItemStack::getCount)
                        .thenComparing(stack -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stack.getItem())))
        );
        resultItem.set(TFCComponents.INGREDIENTS.get(), ItemListComponent.of(inputFood));
        return resultItem;
    }
}

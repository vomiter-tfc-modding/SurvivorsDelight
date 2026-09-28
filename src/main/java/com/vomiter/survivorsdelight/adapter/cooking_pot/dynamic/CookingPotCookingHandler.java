package com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.api.cooking.DynamicCookingRules;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import com.vomiter.survivorsdelight.util.FoodDataBuilder;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
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
        if(!(resultFood instanceof FoodHandler.Dynamic dynamicFood)) return resultItem;
        if (!(recipe instanceof IFluidRequiringRecipe fluidRequiringRecipe)) return resultItem;
        List<ItemStack> inputItems = new ArrayList<>();
        for (int i = 0; i < slotNumber; i++) {
            inputItems.add(inventory.getStackInSlot(i).copyWithCount(1));
        }
        var inputFluid = fluidStack.copy();
        if (!inputFluid.isEmpty()){
            inputFluid.setAmount(fluidRequiringRecipe.sdtfc$getRequiredFluidAmount());
        }
        var context = new DynamicFoodContext<>(
                inputItems, inputFluid, resultItem, recipe, recipe.getId(),
                DynamicFoodContext.Phase.TOTAL, level);
        var foodBuilder = DynamicCookingRules.apply(FoodDataBuilder.from(resultFood.getData()), context);
        SurvivorsDelight.LOGGER.info("Cached Nutrients = {}", foodBuilder.nutrients());

        var inputFood = new ArrayList<>(inputItems.stream().filter(item -> FoodCapability.get(item) != null).toList());
        int hunger = 0;
        for (ItemStack itemStack : inputFood) {
            hunger = Math.max(Objects.requireNonNull(FoodCapability.get(itemStack)).getData().hunger(), hunger);
        }
        var balanceMultiplier = 1f - ((ICookingPotRecipeBalanceFactor)recipe).sdtfc$getBalanceFactor() * inputFood.size();
        foodBuilder.mulNutrient(balanceMultiplier, Nutrient.GRAIN, Nutrient.VEGETABLES, Nutrient.PROTEIN, Nutrient.FRUIT, Nutrient.DAIRY);
        foodBuilder.decayModifier(4.5f);
        foodBuilder.hunger((hunger + 5) / 2);
        dynamicFood.setFood(foodBuilder.build());
        inputFood.sort(
                Comparator.comparing(ItemStack::getCount)
                        .thenComparing(stack -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())))
        );
        dynamicFood.setIngredients(inputFood);
        dynamicFood.setCreationDate(FoodCapability.getRoundedCreationDate());
        return resultItem;
    }


}

package com.vomiter.survivorsdelight.common.device.cooking_pot;

import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.component.item.ItemListComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public interface ICookingPotCalcDynamic {
    ItemStack sdtfc$getCachedDynamic();
    void sdtfc$setCachedDynamic(ItemStack stack);

    default ItemStack calcDynamicResult(RecipeWrapper wrapper, SDCookingPotRecipe recipe, Level level){
        ItemStack result = recipe.getResultStack().copy();
        int resultCount = result.getCount();

        NonNullList<Ingredient> inputItems = NonNullList.create();
        List<ItemStack> foodIngredients = new ArrayList<>();
        FoodData baseFood = FoodData.of(4.5f);
        float[] nutrition = baseFood.nutrients();
        float saturation = baseFood.saturation();
        float water = baseFood.water();
        int foodIngCount = 0;
        int hunger = 0;
        for (int i = 0; i < 9; i++) {
            if (i > 5) continue;
            var stack = wrapper.getItem(i);
            if(!stack.isEmpty()) {
                if(FoodCapability.get(stack) == null) continue;
                foodIngCount ++;
            }
        }
        TagKey<Fluid> MILKS_TAG = Tags.Fluids.MILK;
        var fluidIng = recipe.getFluid() == null? recipe.getFluid(): FluidIngredient.empty();
        if (recipe.getFluidAmountMb() > 0) {
            var fluid = new SizedFluidIngredient(fluidIng, recipe.getFluidAmountMb());
            if(SDUtils.TagUtils.fluidIngredientMatchesTag(level.registryAccess(), fluid, MILKS_TAG)) nutrition[Nutrient.DAIRY.ordinal()] += 1;
        }

        for (int i = 0; i < 9; i++) {
            if(i > 5) continue;
            var stack = wrapper.getItem(i);
            if(!stack.isEmpty()) {
                inputItems.add(Ingredient.of(stack.getItem()));
                if(FoodCapability.get(stack) == null) continue;
                FoodData data = Objects.requireNonNull(FoodCapability.get(stack)).getData();
                foodIngredients.add(stack.getItem().getDefaultInstance());
                for (Nutrient nutrient : Nutrient.VALUES)
                {
                    float extra = 0f;
                    if(stack.is(ModItems.RAW_PASTA.get()) && nutrient.equals(Nutrient.GRAIN)) extra = 1.0f;
                    else if(stack.is(SDTags.ItemTags.TFC_GRAINS) && nutrient.equals(Nutrient.GRAIN)) extra = 1.0f;
                    else if(stack.is(SDTags.ItemTags.create("firmalife", "foods/extra_dough")) && nutrient.equals(Nutrient.GRAIN)) extra = 1.5f;
                    else if(stack.is(SDTags.ItemTags.TFC_DOUGHS) && nutrient.equals(Nutrient.GRAIN)){
                        extra = SDUtils.getExtraNutrientAfterCooking(stack, Nutrient.GRAIN, level) + data.nutrient(nutrient) * 0.2f;
                    }
                    else if(stack.is(SDTags.ItemTags.TFC_RAW_MEATS) && nutrient.equals(Nutrient.PROTEIN)) {
                        extra = SDUtils.getExtraNutrientAfterCooking(stack, Nutrient.PROTEIN, level) + data.nutrient(nutrient) * 0.2f;
                    }
                    nutrition[nutrient.ordinal()] += (data.nutrient(nutrient) * (1f - 0.04f * (float)foodIngCount) + extra) / resultCount;
                }
                water += data.water() / resultCount;
                saturation += data.saturation() / resultCount;
                hunger = Math.max(hunger, data.hunger());
            }
        }

        foodIngredients.sort(Comparator.comparing(ItemStack::getCount)
                .thenComparing(item -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.getItem()))));

        result.set(TFCComponents.INGREDIENTS, ItemListComponent.of(foodIngredients));
        FoodCapability.setFoodForDynamicItemOnCreate(
                result,
                new FoodData(hunger, water, saturation, 0, nutrition, 4.5f));
        return result;
    }
}

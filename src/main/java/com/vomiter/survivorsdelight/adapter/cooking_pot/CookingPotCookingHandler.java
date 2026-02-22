package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.ICookingPotRecipeBridge;
import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.TFCPotRecipeBridgeFD;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.FoodHandler;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.*;

public class CookingPotCookingHandler {
    public static void handleDynamicCookingPotRecipe(
            Level level, BlockPos pos, BlockState state, CookingPotBlockEntity cookingPot, CallbackInfo ci,
            Optional<CookingPotRecipe> recipe
    ){

        if(!((ICookingPotRecipeBridge)cookingPot).sdtfc$getCachedDynamicFoodResult().isEmpty()) return;
        recipe.ifPresent(r -> {
            if(r instanceof TFCPotRecipeBridgeFD) return;
            var originalResult = r.getResultItem(level.registryAccess()).copy();
            if(FoodCapability.get(originalResult) instanceof FoodHandler.Dynamic dynamicFood){
                NonNullList<Ingredient> inputItems = NonNullList.create();
                List<ItemStack> foodIngredients = new ArrayList<>();
                FoodData baseFood = FoodData.decayOnly(4.5f);
                float[] nutrition = baseFood.nutrients();
                float saturation = baseFood.saturation();
                float water = baseFood.water();
                int foodIngCount = 0;
                int hunger = 0;
                int resultCount = r.getResultItem(level.registryAccess()).getCount();
                for (int i = 0; i < cookingPot.getInventory().getSlots(); i++) {
                    if (i > 5) continue;
                    var stack = cookingPot.getInventory().getStackInSlot(i);
                    if(!stack.isEmpty()) {
                        if(FoodCapability.get(stack) == null) continue;
                        foodIngCount ++;
                    }
                }
                TagKey<Fluid> MILKS_TAG = TagKey.create(Registries.FLUID, SDUtils.RLUtils.build("tfc", "milks"));
                var fluid = ((IFluidRequiringRecipe)r).sdtfc$getFluidIngredient();
                if(SDUtils.TagUtils.fluidIngredientMatchesTag(fluid, MILKS_TAG)) nutrition[Nutrient.DAIRY.ordinal()] += 1;

                for (int i = 0; i < cookingPot.getInventory().getSlots(); i++) {
                    if(i > 5) continue;
                    var stack = cookingPot.getInventory().getStackInSlot(i);
                    if(!stack.isEmpty()) {
                        inputItems.add(Ingredient.of(stack.getItem()));
                        if(FoodCapability.get(stack) == null) continue;
                        FoodData data = Objects.requireNonNull(FoodCapability.get(stack)).getData();
                        foodIngredients.add(stack.getItem().getDefaultInstance());
                        for (Nutrient nutrient : Nutrient.VALUES)
                        {
                            float extra = 0f;
                            if(stack.is(ModItems.RAW_PASTA.get()) && nutrient.equals(Nutrient.GRAIN)) extra = 1.0f;
                            else if(stack.is(SDTags.ItemTags.create("tfc", "foods/grains")) && nutrient.equals(Nutrient.GRAIN)) extra = 1.0f;
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
                        .thenComparing(item -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.getItem()))));
                dynamicFood.setIngredients(foodIngredients);
                dynamicFood.setFood(FoodData.create((5 + hunger) / 2, water, saturation, nutrition, 4.5f));
                ((ICookingPotRecipeBridge)cookingPot).sdtfc$setCachedDynamicFoodResult(originalResult);
            }

        });

    }

}

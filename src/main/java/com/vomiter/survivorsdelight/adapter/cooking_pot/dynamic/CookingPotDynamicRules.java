package com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.api.cooking.DynamicCookingRules;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.FoodDataBuilder;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.dries007.tfc.util.Drinkable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class CookingPotDynamicRules {
    /** @deprecated Use {@link DynamicCookingRules.PotFoodModifier}. */
    @Deprecated(forRemoval = false)
    @FunctionalInterface
    public interface PotFoodModifier {
        FoodDataBuilder modify(
                FoodDataBuilder food,
                DynamicFoodContext<CookingPotRecipe> context
        );
    }

    /** @deprecated Use {@link DynamicCookingRules.RuleHolder}. */
    @Deprecated(forRemoval = false)
    public record RuleHolder(int priority, ResourceLocation id, PotFoodModifier modifier){}

    /**
     * Compatibility bridge for existing addons. Registration timing, duplicate
     * IDs and ordering follow the public API.
     * @deprecated Use {@link DynamicCookingRules#register(DynamicCookingRules.RuleHolder)}.
     */
    @Deprecated(forRemoval = false)
    public static void register(RuleHolder rule){
        Objects.requireNonNull(rule, "Rule");
        DynamicCookingRules.register(rule.id(), rule.priority(),
                Objects.requireNonNull(rule.modifier(), "Rule modifier")::modify);
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(DynamicCookingRules::freeze);
    }

    public static FoodData getBuiltInFoodData(Food food){
        return FoodCapability.get(SDUtils.getTFCFoodItem(food).getDefaultInstance()).getData();
    }

    public static @Nullable FoodData getHeatedFoodData(ItemStack stack, Level level){
        Optional<HeatingRecipe> heatingRecipe = level.getRecipeManager()
                .getRecipeFor(TFCRecipeTypes.HEATING.get(), new ItemStackInventory(stack.copy()), level);
        if (heatingRecipe.isPresent()){
            var recipe = heatingRecipe.get();
            var heatingResult = recipe.assemble(new ItemStackInventory(stack.copy()), level.registryAccess());
            if (heatingResult != null && !heatingRecipe.isEmpty()){
                var food = FoodCapability.get(heatingResult);
                if (food != null) return food.getData();
            }
        }
        if (FoodCapability.get(stack)!=null){
            return Objects.requireNonNull(FoodCapability.get(stack)).getData();
        }
        return null;
    }

    public static void onCommonSetup(FMLCommonSetupEvent event){
        event.enqueueWork(() -> {
            DynamicCookingRules.register(new DynamicCookingRules.RuleHolder(
                    DynamicCookingRules.PRIORITY_ADDITION,
                    SurvivorsDelight.modLoc("heating"),
                    ((food, context) -> {
                        if(context.phase().equals(DynamicFoodContext.Phase.TOTAL)) return food;
                        var heated = getHeatedFoodData(context.stack(), context.level());
                        if(heated == null) return food;
                        var heatedFood = FoodDataBuilder.from(heated).mulNutrient(1.2f, Nutrient.PROTEIN, Nutrient.GRAIN);
                        return food.addBuilder(heatedFood);
                    })
            ));

            DynamicCookingRules.register(new DynamicCookingRules.RuleHolder(
                    DynamicCookingRules.PRIORITY_ADDITION,
                    SurvivorsDelight.modLoc("bone"),
                    ((food, context) -> {
                        if(context.phase().equals(DynamicFoodContext.Phase.TOTAL)) return food;
                        if(!context.stack().is(Items.BONE)) return food;
                        return food.addNutrient(Nutrient.DAIRY, 0.25f);
                    })
            ));


            DynamicCookingRules.register(new DynamicCookingRules.RuleHolder(
                    DynamicCookingRules.PRIORITY_ADDITION,
                    SurvivorsDelight.modLoc("pasta_and_grains"),
                    ((food, context) -> {
                        if(context.phase().equals(DynamicFoodContext.Phase.TOTAL)) return food;
                        if (context.stack().is(ModItems.RAW_PASTA.get())||context.stack().is(SDTags.ItemTags.TFC_GRAINS)){
                            return food.addData(Objects.requireNonNull(getHeatedFoodData(
                                    SDUtils.getTFCFoodItem(Food.BARLEY_DOUGH).getDefaultInstance(),
                                    context.level()
                            )));
                        } else return food;
                    })
            ));

            DynamicCookingRules.register(
                    new DynamicCookingRules.RuleHolder(
                            DynamicCookingRules.PRIORITY_MULTIPLICATION,
                            SurvivorsDelight.modLoc("sweetener"),
                            ((food, context) -> {
                                if(context.phase().equals(DynamicFoodContext.Phase.TOTAL)) return food;
                                if(!context.stack().is(SDTags.ItemTags.TFC_SWEETENER)) return food;
                                return food.mulNutrient(2, Nutrient.VEGETABLES, Nutrient.FRUIT);
                            })
                    )
            );

            DynamicCookingRules.register(
                    new DynamicCookingRules.RuleHolder(
                            DynamicCookingRules.PRIORITY_MULTIPLICATION,
                            SurvivorsDelight.modLoc("salt"),
                            ((food, context) -> {
                                if(context.phase().equals(DynamicFoodContext.Phase.TOTAL)) return food;
                                if(!context.stack().is(TFCItems.POWDERS.get(Powder.SALT).get())) return food;
                                return food.mulNutrient(1.2f, Nutrient.PROTEIN);
                            })
                    )
            );

            DynamicCookingRules.register(
                    new DynamicCookingRules.RuleHolder(
                            DynamicCookingRules.PRIORITY_FLUID,
                            SurvivorsDelight.modLoc("oil"),
                            ((food, context) -> {
                                if (context.phase().equals(DynamicFoodContext.Phase.INDIVIDUAL)) return food;
                                if (!context.inputFluids().stream().anyMatch(fluidStack -> fluidStack.getFluid().defaultFluidState().is(SDTags.FluidTags.COOKING_OILS))) return food;
                                if(context.stack().is(SDTags.ItemTags.FEAST_BLOCKS)){
                                    return food
                                            .mulNutrient(1.5f, Nutrient.PROTEIN)
                                            .mulNutrient(3, Nutrient.VEGETABLES, Nutrient.FRUIT, Nutrient.GRAIN);
                                }
                                else {
                                    return food
                                            .mulNutrient(1.1f, Nutrient.PROTEIN)
                                            .mulNutrient(1.3f, Nutrient.VEGETABLES, Nutrient.FRUIT, Nutrient.GRAIN);
                                }
                            })
                    )
            );

            DynamicCookingRules.register(
                    new DynamicCookingRules.RuleHolder(
                            DynamicCookingRules.PRIORITY_FLUID,
                            SurvivorsDelight.modLoc("milk"),
                            ((food, context) -> {
                                if (context.phase().equals(DynamicFoodContext.Phase.INDIVIDUAL)) return food;
                                if (context.inputFluids().stream().noneMatch(fluidStack -> fluidStack.getFluid().defaultFluidState().is(SDTags.FluidTags.TFC_MILKS))) return food;
                                var milkFluid = context.inputFluids().stream().filter(fluidStack -> fluidStack.getFluid().defaultFluidState().is(SDTags.FluidTags.TFC_MILKS)).findFirst().orElseThrow();
                                var milk = Drinkable.get(milkFluid.getFluid());
                                if (milk == null || milk.getFoodStats() == null) return food;
                                var multiplier = milkFluid.getAmount() / 25;
                                var milkNutrients = FoodDataBuilder.from(milk.getFoodStats()).mul(multiplier);
                                return food.addBuilder(milkNutrients);
                            })
                    )
            );

        });
    }
}

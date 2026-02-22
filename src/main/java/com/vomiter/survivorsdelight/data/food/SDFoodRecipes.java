package com.vomiter.survivorsdelight.data.food;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.capabilities.food.FoodData;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.dries007.tfc.common.fluids.Alcohol;
import net.dries007.tfc.common.fluids.SimpleFluid;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.Powder;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.Set;
import java.util.function.Consumer;

public class SDFoodRecipes {
    TagKey<Fluid> milks = TagKey.create(Registries.FLUID, SDUtils.RLUtils.build("tfc", "milks"));
    TagKey<Fluid> oils = SDTags.FluidTags.COOKING_OILS;

    
    private SDFoodAndRecipeGenerator.CookingBuilder cook(String id, ItemLike outItem, int count, int time, double exp) {
        return SurvivorsDelight.foodAndCookingGenerator.cooking(id, outItem, count, time, (float) exp, null);
    }

    private SDFoodAndRecipeGenerator.CookingBuilder cook(String id, ItemLike outItem, int count, int time, double exp, ItemLike container) {
        return SurvivorsDelight.foodAndCookingGenerator.cooking(id, outItem, count, time, (float) exp, container);
    }

    private SDFoodAndRecipeGenerator.ShapedCraftingBuilder craft(String id, ItemLike outItem, int count){
        return SurvivorsDelight.foodAndCookingGenerator.crafting(id, outItem, count);
    }

    private SDFoodDataProvider.Builder buildFood(String id){
        return SurvivorsDelight.foodAndCookingGenerator.provider().newBuilder(id);
    }

    private FoodData getTFCFoodData(Food food){
        return SurvivorsDelight.foodAndCookingGenerator.provider().readTfcFoodJson(food);
    }

    private FoodData getOtherFoodData(Item food){
        return SDFoodAndRecipeGenerator.foodDataMap.get(food);
    }

    public void save(Consumer<FinishedRecipe> out){
        new SDBasicFoodData().save();
        drinks(out);
        pie(out);
        soup(out);
        meal(out);
        feast(out);
        smallFood(out);
        buildFood("pet_food/horse_feed").item(ModItems.HORSE_FEED.get()).setDecay(1).save();
        buildFood("pet_food/dog_food").item(ModItems.DOG_FOOD.get()).setDecay(3).save();
    }

    public void smallFood(Consumer<FinishedRecipe> out){
        craft("food/barbecue_stick", ModItems.BARBECUE_STICK.get(), 1)
                .shape("MV", "VS")
                .defineFood('M', SDTags.ItemTags.TFC_COOKED_MEATS)
                .defineFood('V', SDTags.ItemTags.TFC_VEGETABLES)
                .defineNonFood('S', SDTags.ItemTags.create("forge", "rods/wooden"))
                .build(out)
                .saveFoodData();

        craft("food/cod_roll", ModItems.COD_ROLL.get(), 1)
                .shape("F", "R")
                .defineFood('F', ModItems.COD_SLICE.get())
                .defineFood('R', Food.COOKED_RICE)
                .factorPerIngredient(0)
                .build(out)
                .saveFoodData();

        craft("food/salmon_roll", ModItems.SALMON_ROLL.get(), 1)
                .shape("F", "R")
                .defineFood('F', ModItems.SALMON_SLICE.get())
                .defineFood('R', Food.COOKED_RICE)
                .factorPerIngredient(0)
                .build(out)
                .saveFoodData();

        cook("food/dumplings", ModItems.DUMPLINGS.get(), 2, 200, 1)
                .food(SDTags.ItemTags.TFC_DOUGHS, getTFCFoodData(Food.BARLEY_BREAD))
                .food(SDTags.ItemTags.TFC_RAW_MEATS)
                .food(Food.ONION)
                .food(Food.CABBAGE)
                .build(out)
                .saveFoodData();

        cook("food/cabbage_rolls", ModItems.CABBAGE_ROLLS.get(), 1, 200, 1)
                .food(Food.CABBAGE)
                .food(ModTags.CABBAGE_ROLL_INGREDIENTS)
                .build(out)
                .saveFoodData();

        cook("food/stuffed_potato", ModItems.STUFFED_POTATO.get(), 1, 200, 1)
                .food(Food.BAKED_POTATO)
                .food(SDTags.ItemTags.TFC_RAW_MEATS)
                .fluid(milks, 100)
                .build(out)
                .saveFoodData();

    }

    /* ---------------------- FEAST ---------------------- */
    public void feast(Consumer<FinishedRecipe> out){

        cook("feast/shepherds_pie", ModItems.SHEPHERDS_PIE_BLOCK.get(), 1, 1200, 5, Items.BOWL)
                .food(SDTags.ItemTags.MEATS_FOR_SHEPHERDS_PIE)
                .food(SDTags.ItemTags.MEATS_FOR_SHEPHERDS_PIE)
                .food(SDTags.ItemTags.MEATS_FOR_SHEPHERDS_PIE)
                .food(SDTags.ItemTags.TFC_DOUGHS)
                .food(TFCItems.SALADS.get(Nutrient.VEGETABLES).get())
                .fluid(milks, 100)
                .build(out)
                .saveFoodData();

        cook("feast/honey_glazed_ham", ModItems.HONEY_GLAZED_HAM_BLOCK.get(), 1, 1200, 5, Items.BOWL)
                .nonfood(SDTags.ItemTags.TFC_SWEETENER)
                .food(ModItems.SMOKED_HAM.get())
                .food(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)
                .food(SDTags.ItemTags.TFC_DOUGHS)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("feast/honey_glazed_ham2", ModItems.HONEY_GLAZED_HAM_BLOCK.get(), 1, 1200, 5, Items.BOWL)
                .nonfood(SDTags.ItemTags.TFC_SWEETENER)
                .food(ModItems.SMOKED_HAM.get())
                .food(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)
                .food(Ingredient.merge(Set.of(
                        Ingredient.of(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE),
                        Ingredient.of(SDTags.ItemTags.TFC_VEGETABLES)))
                )
                .food(SDTags.ItemTags.TFC_DOUGHS)
                .fluid(oils, 100)
                .build(out);

        cook("feast/stuffed_pumpkin", ModItems.STUFFED_PUMPKIN_BLOCK.get(), 1, 1200, 5, Items.CARVED_PUMPKIN)
                .food(Items.BROWN_MUSHROOM)
                .food(SDTags.ItemTags.TFC_GRAINS)
                .food(SDTags.ItemTags.TFC_FRUITS)
                .food(SDTags.ItemTags.TFC_VEGETABLES)
                .food(Food.ONION)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("feast/roasted_chicken", ModItems.ROAST_CHICKEN_BLOCK.get(), 1, 1200, 5, Items.BOWL)
                .food(SDTags.ItemTags.RAW_POULTRY)
                .food(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)
                .food(SDTags.ItemTags.TFC_VEGETABLES)
                .food(SDTags.ItemTags.TFC_GRAINS)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();
    }

    /* ---------------------- MEAL ---------------------- */
    public void meal(Consumer<FinishedRecipe> out){
        var oils = TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource();

        cook("meal/fried_rice", ModItems.FRIED_RICE.get(), 1, 600, 3, Items.BOWL)
                .food(Food.COOKED_RICE)
                .food(Food.COOKED_EGG)
                .food(Food.ONION)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/bacon_and_eggs", ModItems.BACON_AND_EGGS.get(), 1, 300, 3, Items.BOWL)
                .food(Food.COOKED_EGG)
                .food(Food.COOKED_EGG)
                .food(ModItems.COOKED_BACON.get(), FoodData.EMPTY)
                .food(ModItems.COOKED_BACON.get(), getTFCFoodData(Food.COOKED_PORK))
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/pasta_with_meatballs", ModItems.PASTA_WITH_MEATBALLS.get(), 1, 600, 3, Items.BOWL)
                .food(ModItems.TOMATO_SAUCE.get())
                .food(ModItems.RAW_PASTA.get())
                .food(ModItems.BEEF_PATTY.get())
                .food(ModItems.BEEF_PATTY.get())
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/pasta_with_mutton_chop", ModItems.PASTA_WITH_MUTTON_CHOP.get(), 1, 600, 3, Items.BOWL)
                .food(ModItems.TOMATO_SAUCE.get())
                .food(ModItems.RAW_PASTA.get())
                .food(TFCItems.FOOD.get(Food.COOKED_MUTTON).get())
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/roasted_mutton_chops", ModItems.ROASTED_MUTTON_CHOPS.get(), 1, 1200, 3, Items.BOWL)
                .food(Food.MUTTON)
                .food(Food.TOMATO)
                .food(SDTags.ItemTags.create("tfc", "foods/grains"), getTFCFoodData(Food.COOKED_RICE))
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/vegetable_noodles", ModItems.VEGETABLE_NOODLES.get(), 1, 1200, 3, Items.BOWL)
                .food(ModItems.RAW_PASTA.get())
                .food(SDTags.ItemTags.create("tfc", "foods/vegetables"))
                .food(SDTags.ItemTags.create("tfc", "foods/vegetables"))
                .food(SDTags.ItemTags.create("tfc", "foods/vegetables"))
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/steak_and_potatoes", ModItems.STEAK_AND_POTATOES.get(), 1, 600, 3, Items.BOWL)
                .food(Food.COOKED_BEEF)
                .food(Food.BAKED_POTATO)
                .food(Food.ONION)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/ratatouille", ModItems.RATATOUILLE.get(), 1, 1200, 3, Items.BOWL)
                .food(ModItems.TOMATO_SAUCE.get())
                .food(Food.ONION)
                .food(SDTags.ItemTags.create("tfc", "foods/vegetables"))
                .food(SDTags.ItemTags.create("tfc", "foods/vegetables"))
                .food(SDTags.ItemTags.create("tfc", "foods/vegetables"))
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/squid_ink_pasta", ModItems.SQUID_INK_PASTA.get(), 1, 1200, 3, Items.BOWL)
                .food(ModItems.RAW_PASTA.get(), getTFCFoodData(Food.COOKED_RICE))
                .nonfood(Items.INK_SAC)
                .food(Food.COOKED_CALAMARI)
                .food(Food.GARLIC)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/grilled_salmon", ModItems.GRILLED_SALMON.get(), 1, 600, 3, Items.BOWL)
                .food(Food.COOKED_SALMON)
                .food(Food.ONION)
                .food(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)
                .fluid(oils, 100)
                .build(out)
                .saveFoodData();

        cook("meal/mushroom_rice", ModItems.MUSHROOM_RICE.get(), 1, 1200, 3, Items.BOWL)
                .food(Food.RICE_GRAIN)
                .food(Items.RED_MUSHROOM)
                .food(Items.BROWN_MUSHROOM)
                .food(ModItems.BONE_BROTH.get())
                .build(out)
                .saveFoodData();
    }

    /* ---------------------- SOUP ---------------------- */
    public void soup(Consumer<FinishedRecipe> out){
        var TFCSoups = SDTags.ItemTags.create("tfc", "soups");
        var vegetableAndFruitSoup = Ingredient.of(
                TFCItems.SOUPS.get(Nutrient.VEGETABLES).get(),
                TFCItems.SOUPS.get(Nutrient.FRUIT).get()
        );
        TagKey<Fluid> MILKS_TAG = TagKey.create(Registries.FLUID, SDUtils.RLUtils.build("tfc", "milks"));
        cook("soup/bone_broth", ModItems.BONE_BROTH.get(), 4, 3600, 3, Items.BOWL)
                .nonfood(Items.BONE.asItem())
                .nonfood(Items.BONE.asItem())
                .nonfood(TFCItems.POWDERS.get(Powder.SALT).get())
                .food(TFCItems.FOOD.get(Food.GARLIC).get(), FoodData.EMPTY)
                .food(TFCItems.FOOD.get(Food.GARLIC).get(), FoodData.EMPTY)
                .food(TFCItems.FOOD.get(Food.ONION).get(), FoodData.EMPTY)
                .fluid(TFCTags.Fluids.ANY_FRESH_WATER, 400)
                .build(out)
                .getFoodData()
                .setDairy(1)
                .setDecay(0.5)
                .setWater(5)
                .save();

        cook("soup/tomato_sauce", ModItems.TOMATO_SAUCE.get(), 1, 600, 2, Items.BOWL)
                .food(Food.TOMATO)
                .food(Food.TOMATO)
                .build(out)
                .saveFoodData();

        cook("soup/pumpkin_soup", ModItems.PUMPKIN_SOUP.get(), 1, 600, 3, Items.BOWL)
                .food(TFCItems.SOUPS.get(Nutrient.PROTEIN).get())
                .food(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get())
                .fluid(MILKS_TAG, 100)
                .build(out)
                .saveFoodData();

        cook("soup/baked_cod_stew", ModItems.BAKED_COD_STEW.get(), 1, 600, 3, Items.BOWL)
                .food(SDTags.ItemTags.create("forge", "cooked_fishes/cod"))
                .food(TFCSoups)
                .food(ModItems.BONE_BROTH.get())
                .build(out)
                .saveFoodData();

        cook("soup/chicken_soup", ModItems.CHICKEN_SOUP.get(), 1, 600, 3, Items.BOWL)
                .food(SDTags.ItemTags.create("forge", "cooked_chicken"))
                .food(TFCSoups)
                .food(ModItems.BONE_BROTH.get())
                .build(out)
                .saveFoodData();

        cook("soup/fish_stew", ModItems.FISH_STEW.get(), 1, 900, 3, Items.BOWL)
                .food(SDTags.ItemTags.FISHES_USABLE_IN_STEW)
                .food(vegetableAndFruitSoup)
                .build(out)
                .saveFoodData();

        cook("soup/vegetable_soup", ModItems.VEGETABLE_SOUP.get(), 1, 900, 3, Items.BOWL)
                .food(ModItems.BONE_BROTH.get())
                .food(TFCItems.SOUPS.get(Nutrient.VEGETABLES).get())
                .build(out)
                .saveFoodData();

        cook("soup/beef_stew", ModItems.BEEF_STEW.get(), 1, 900, 3, Items.BOWL)
                .food(vegetableAndFruitSoup)
                .food(TFCItems.FOOD.get(Food.COOKED_BEEF).get())
                .nonfood(TFCItems.POWDERS.get(Powder.SALT).get())
                .nonfood(TFCItems.POWDERS.get(Powder.SALT).get())
                .build(out)
                .saveFoodData();

        cook("soup/noodle_soup", ModItems.NOODLE_SOUP.get(), 1, 600, 3, Items.BOWL)
                .food(TFCSoups)
                .food(ModItems.RAW_PASTA.get(), getTFCFoodData(Food.COOKED_RICE))
                .food(ModItems.BONE_BROTH.get())
                .build(out)
                .saveFoodData();

        cook("soup/mushroom_stew", Items.MUSHROOM_STEW, 1, 1200, 3, Items.BOWL)
                .food(Items.BROWN_MUSHROOM)
                .food(Items.RED_MUSHROOM)
                .nonfood(TFCItems.POWDERS.get(Powder.SALT).get())
                .build(out)
                .saveFoodData();

    }

    /* ---------------------- PIE ---------------------- */
    public void pie(Consumer<FinishedRecipe> out){
        cook("pie/pie_crust", ModItems.PIE_CRUST.get(), 1, 1200, 2)
                .food(SDTags.ItemTags.TFC_DOUGHS, getTFCFoodData(Food.BARLEY_BREAD))
                .food(SDTags.ItemTags.TFC_DOUGHS, getTFCFoodData(Food.BARLEY_BREAD))
                .food(SDTags.ItemTags.PIE_CRUST_DAIRY, getTFCFoodData(Food.CHEESE))
                .fluid(TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.OLIVE_OIL).getSource(), 200)
                .build(out)
                .getFoodData()
                .setDairy(1)
                .save();

        cook("pie/apple_pie", ModItems.APPLE_PIE.get(), 1, 1200, 2, ModItems.PIE_CRUST.get())
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .nonfood(SDTags.ItemTags.TFC_SWEETENER)
                .nonfood(SDTags.ItemTags.TFC_SWEETENER)
                .nonfood(SDTags.ItemTags.create("tfc", "makes_red_dye"))
                .build(out)
                .getFoodData()
                .addNutrientsAndSetMaxHunger(SDFoodAndRecipeGenerator.foodDataMap.get(ModItems.PIE_CRUST.get()), 1)
                .save();

        craft("pie/berry_cheesecake", ModItems.SWEET_BERRY_CHEESECAKE.get(), 1)
                .shape("FFF", "SCS", "SPS")
                .defineFood('F', SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)
                .defineFood('C', SDTags.ItemTags.CHEESE_FOR_CHEESECAKE, getTFCFoodData(Food.CHEESE))
                .defineNonFood('S', SDTags.ItemTags.TFC_SWEETENER)
                .defineFood('P', ModItems.PIE_CRUST.get())
                .build(out)
                .saveFoodData();

        craft("pie/chocolate_pie", ModItems.CHOCOLATE_PIE.get(), 1)
                .shape("ccc", "SCS", "SPS")
                .defineFood('c', SDTags.ItemTags.CHOCOLATE_FOR_CHEESECAKE, new FoodData(4, 0, 0.3f, 0.5f, 0, 0, 0, 0.5f, 0.3f))
                .defineFood('C', SDTags.ItemTags.CHEESE_FOR_CHEESECAKE, getTFCFoodData(Food.CHEESE))
                .defineNonFood('S', SDTags.ItemTags.TFC_SWEETENER)
                .defineFood('P', ModItems.PIE_CRUST.get())
                .build(out)
                .saveFoodData();

    }

    /* ---------------------- DRINKS ---------------------- */
    public void drinks(Consumer<FinishedRecipe> out){
        appleCider(out);
        melonJuice(out);
        hotCocoa(out);
    }

    public void hotCocoa(Consumer<FinishedRecipe> out){
        TagKey<Fluid> MILKS_TAG = TagKey.create(Registries.FLUID, SDUtils.RLUtils.build("tfc", "milks"));
        FoodData empty = FoodData.EMPTY;

        cook("drink/hot_cocoa", ModItems.HOT_COCOA.get(), 1, 1200, 2, Items.GLASS_BOTTLE)
                .food(SDTags.ItemTags.COCOA_POWDER, empty)
                .food(SDTags.ItemTags.COCOA_POWDER, empty)
                .nonfood(net.minecraft.world.item.crafting.Ingredient.of(SDTags.ItemTags.create("tfc", "sweetener")))
                .fluid(MILKS_TAG, 200)
                .whenModLoaded("firmalife")
                .build(out)
                .getFoodData()
                .setDecay(5)
                .setDairy(1.5)
                .setHunger(0)
                .setSaturation(0)
                .setWater(10)
                .save();
    }

    public void appleCider(Consumer<FinishedRecipe> out) {
        cook("drink/apple_cider", ModItems.APPLE_CIDER.get(), 1, 1200, 2, Items.GLASS_BOTTLE)
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .food(SDTags.ItemTags.APPLE_FOR_CIDER, getTFCFoodData(Food.RED_APPLE))
                .nonfood(SDTags.ItemTags.create("tfc", "sweetener"))
                .fluid(TFCFluids.ALCOHOLS.get(Alcohol.CIDER).getSource(), 400)
                .build(out)
                .getFoodData()
                .addNutrientsAndSetMaxHunger(Food.RED_APPLE, 0.8f)
                .setWater(5)
                .setHunger(1)
                .setSaturation(0)
                .setDecay(1.7)
                .save();
    }

    public void melonJuice(Consumer<FinishedRecipe> out){
        cook("drink/melon_juice", ModItems.MELON_JUICE.get(), 1, 1200, 2, Items.GLASS_BOTTLE)
                .factorPerIngredient(0f)
                .food(Food.MELON_SLICE)
                .food(Food.MELON_SLICE)
                .food(Food.MELON_SLICE)
                .food(Food.MELON_SLICE)
                .nonfood(TFCItems.POWDERS.get(Powder.SALT).get())
                .build(out)
                .getFoodData()
                .setSaturation(0)
                .setHunger(1)
                .setDecay(2)
                .save();
    }
}

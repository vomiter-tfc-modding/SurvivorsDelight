package com.vomiter.survivorsdelight.data.food;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.items.Food;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SDBasicFoodData {
    final SDFoodDataProvider provider;
    final static Map<Item, FoodData> FALL_BACK = new LinkedHashMap<>();

    public SDBasicFoodData(){
        this.provider = SurvivorsDelight.foodAndCookingGenerator.provider();
    }

    public void save(){
        fishRoll();
        mushrooms();
        cutFood();
        provider.newBuilder("slices_and_servings")
                .tag(SDTags.ItemTags.SLICES_AND_SERVINGS.location().toString())
                .type("dynamic")
                .save();
    }

    public void mushrooms(){
        provider.newBuilder("mushrooms/general")
                .ingredient(
                        SDUtils.ingredientToJsonElement(Ingredient.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, ModItems.RED_MUSHROOM_COLONY.get(), ModItems.BROWN_MUSHROOM_COLONY.get()))
                )
                .setDecay(5)
                .save();

        provider.newBuilder("mushrooms/brown_mushroom")
                .item(Items.BROWN_MUSHROOM)
                .setSaturation(1)
                .setDairy(0.5)
                .setVegetables(0.5)
                .save();
    }



    public void fishRoll(){
        var cod = provider.readTfcFoodJson(Food.COD);
        var salmon = provider.readTfcFoodJson(Food.SALMON);
        var cooked_rice = provider.readTfcFoodJson(Food.COOKED_RICE);

        provider.newBuilder("food/cod_roll").item(ModItems.COD_ROLL.get())
                .slicedFrom(Food.COD, 2)
                .setGrain(cooked_rice.nutrient(Nutrient.GRAIN))
                .setHunger(cooked_rice.hunger() + Math.round(cod.hunger()/2f))
                .setSaturation(Math.max(cooked_rice.saturation(), cod.saturation()))
                .save();

        provider.newBuilder("food/salmon_roll").item(ModItems.SALMON_ROLL.get())
                .slicedFrom(Food.SALMON, 2)
                .setGrain(cooked_rice.nutrient(Nutrient.GRAIN))
                .setHunger(cooked_rice.hunger() + Math.round(salmon.hunger()/2f))
                .setSaturation(Math.max(cooked_rice.saturation(), salmon.saturation()))
                .save();
    }

    public static List<CutSpec> cutSpecs = List.of(
            cut2(ModItems.CABBAGE_LEAF, Food.CABBAGE),
            cut2(ModItems.MINCED_BEEF, Food.BEEF),
            cut2(ModItems.BEEF_PATTY, Food.COOKED_BEEF),
            cut2(ModItems.CHICKEN_CUTS, Food.CHICKEN),
            cut2(ModItems.COOKED_CHICKEN_CUTS, Food.COOKED_CHICKEN),
            cut2(ModItems.BACON, Food.PORK),
            cut2(ModItems.COOKED_BACON, Food.COOKED_PORK),
            cut2(ModItems.COD_SLICE, Food.COD),
            cut2(ModItems.COOKED_COD_SLICE, Food.COOKED_COD),
            cut2(ModItems.SALMON_SLICE, Food.SALMON),
            cut2(ModItems.COOKED_SALMON_SLICE, Food.COOKED_SALMON),
            cut2(ModItems.MUTTON_CHOPS, Food.MUTTON),
            cut2(ModItems.COOKED_MUTTON_CHOPS, Food.COOKED_MUTTON)
    );

    public void cutFood() {
        provider
                .newBuilder("cut/raw_pasta")
                .item(ModItems.RAW_PASTA.get())
                .from(provider.readTfcFoodJson(Food.BARLEY_DOUGH))
                .save();

        registerCuts(cutSpecs, provider);

        provider.newBuilder("ham").item(ModItems.HAM.get()).multipliedFrom(Food.PORK, 2).addNutrientsAndSetMaxHunger(Food.PORK, 0.5f).save();
        provider.newBuilder("smoked_ham").item(ModItems.SMOKED_HAM.get()).multipliedFrom(Food.COOKED_PORK, 2).addNutrientsAndSetMaxHunger(Food.COOKED_PORK, 0.5f).save();
    }

    public record CutSpec(Supplier<? extends Item> item, Food from, int slices) {}

    public static CutSpec cut2(Supplier<? extends Item> item, Food from) {
        return new CutSpec(item, from, 2);
    }

    private void registerCuts(List<CutSpec> defs, SDFoodDataProvider provider) {
        for (var spec : defs) {
            final Item item = spec.item().get();
            final ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (key == null) {
                throw new IllegalStateException("Unregistered item: " + item);
            }
            final String builderId = "cut/" + key.getPath(); // 規則：cut/ + item path

            provider
                    .newBuilder(builderId)
                    .item(item)
                    .slicedFrom(provider.readTfcFoodJson(spec.from()), spec.slices())
                    .save();
        }
    }
}

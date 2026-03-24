package com.vomiter.survivorsdelight.data.food;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.recipe.builder.SDCuttingRecipeBuilder;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.outputs.CopyFoodModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.CommonTags;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class SDFoodCuttingRecipes {
    public static final List<Food> SALMON_LIKE = List.of(Food.SALMON, Food.LAKE_TROUT, Food.RAINBOW_TROUT);
    public static final List<Food> COD_LIKE = List.of(Food.COD, Food.CRAPPIE, Food.LARGEMOUTH_BASS, Food.SMALLMOUTH_BASS);
    public static final Map<Food, List<Food>> FISH_TO_CUT = Map.of(Food.SALMON, SALMON_LIKE, Food.COD, COD_LIKE);

    public void cut2(RecipeOutput out) {
        final Ingredient knife = Ingredient.of(CommonTags.TOOLS_KNIFE);

        SDBasicFoodData.cutSpecs.forEach(cutSpec -> {
            final Food fromFood = cutSpec.from();
            final List<Food> fishType = FISH_TO_CUT.get(fromFood);

            final Ingredient ingredient;
            if (fishType != null) {
                Stream<Food> fishStream = fishType.stream();
                if (fromFood.name().contains("COOKED")) {
                    fishStream = fishStream.map(f -> Food.valueOf("COOKED_" + f.name()));
                }
                ingredient = Ingredient.of(fishStream.map(f -> TFCItems.FOOD.get(f).get().getDefaultInstance()));
            } else {
                ingredient = Ingredient.of(TFCItems.FOOD.get(fromFood).get());
            }

            // 使用 addResult(ItemStackProvider)
            SDCuttingRecipeBuilder.cuttingNotRotten(ingredient)
                .tool(knife)
                .addResult(ItemStackProvider.of(
                    new ItemStack(cutSpec.item().get(), cutSpec.slices()),
                    CopyFoodModifier.INSTANCE
                ))
                .save(out, SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cutting/food/" + BuiltInRegistries.ITEM.getKey(cutSpec.item().get()).getPath()));
        });

        SDCuttingRecipeBuilder.cuttingNotRotten(Ingredient.of(ModItems.HAM.get()))
            .tool(knife)
            .addResult(ItemStackProvider.of(new ItemStack(SDUtils.getTFCFoodItem(Food.PORK), 2), CopyFoodModifier.INSTANCE))
            .addResult(ItemStackProvider.of(new ItemStack(Items.BONE)))
            .save(out, SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cutting/food/ham"));

        SDCuttingRecipeBuilder.cuttingNotRotten(Ingredient.of(ModItems.SMOKED_HAM.get()))
            .tool(knife)
            .addResult(ItemStackProvider.of(new ItemStack(SDUtils.getTFCFoodItem(Food.COOKED_PORK), 2), CopyFoodModifier.INSTANCE))
            .addResult(ItemStackProvider.of(new ItemStack(Items.BONE)))
            .save(out, SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cutting/food/smoked_ham"));

        SDCuttingRecipeBuilder.cuttingNotRotten(Ingredient.of(SDTags.ItemTags.TFC_DOUGHS))
            .tool(knife)
            .addResult(ItemStackProvider.of(new ItemStack(ModItems.RAW_PASTA.get(), 1), CopyFoodModifier.INSTANCE))
            .save(out, SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cutting/food/raw_pasta"));
    }
}

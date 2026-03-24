package com.vomiter.survivorsdelight.data.recipe;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.food.SDFoodRecipes;
import com.vomiter.survivorsdelight.data.food.SDFoodCuttingRecipes;
import com.vomiter.survivorsdelight.data.recipe.cutting.WoodCuttingRecipes;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SDRecipeProvider extends RecipeProvider {
    WoodCuttingRecipes woodCuttingRecipes = new WoodCuttingRecipes();
    SDCraftingRecipes craftingRecipes = new SDCraftingRecipes();
    SDFoodRecipes cookingPotRecipes = new SDFoodRecipes();
    SDFoodCuttingRecipes foodCuttingRecipes = new SDFoodCuttingRecipes();
    SDAnvilAndWeldingRecipes anvilRecipes = new SDAnvilAndWeldingRecipes();
    SDHeatingRecipes heatingRecipes;

    public SDRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper exh) {
        super(output, registries);
        SurvivorsDelight.foodAndCookingGenerator.injectPackOutput(output);
        heatingRecipes = new SDHeatingRecipes(exh);
    }

    @Override
    protected void buildRecipes(@NotNull RecipeOutput out) {
        TFCBlocks.WOODS.forEach((wood, blockTypes) -> {
            woodCuttingRecipes.stripForBark(wood, out);
            woodCuttingRecipes.salvageWoodFurniture(wood, out);
            craftingRecipes.cabinetForWood(wood, out);
            Arrays.stream(Metal.values()).filter(Metal::allParts).forEach(
                    m -> woodCuttingRecipes.salvageHangingSign(wood, m, out)
            );
        });
        craftingRecipes.save(out);
        cookingPotRecipes.save(out);
        foodCuttingRecipes.cut2(out);
        anvilRecipes.save(out);
        heatingRecipes.save(out);
    }
}

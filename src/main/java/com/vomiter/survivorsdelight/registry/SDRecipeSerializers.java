package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.registry.recipe.NutrientShapedRecipe;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import com.vomiter.survivorsdelight.registry.recipe.SDCuttingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public final class SDRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SurvivorsDelight.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NutrientShapedRecipe>> NUTRITION_CRAFTING =
            SERIALIZERS.register("nutrition_crafting", NutrientShapedRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SDCuttingRecipe>> SD_CUTTING =
            SERIALIZERS.register("sd_cutting", SDCuttingRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<SDCookingPotRecipe>>
            SD_COOKING_POT = SERIALIZERS.register(
            "sd_cooking_pot",
            // 可以直接回傳你 enum 的 INSTANCE
            () -> SDCookingPotRecipe.Serializer.INSTANCE
    );

    private SDRecipeSerializers() {}
}

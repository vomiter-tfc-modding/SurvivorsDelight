package com.vomiter.survivorsdelight.data.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;

public final class FDRecipeBlocker {

    private static final List<String> OTHER_BLOCKING_ID = new ArrayList<>(List.of(
            "canvas"
    ));

    public static void addOtherBlockingId(String path){
        OTHER_BLOCKING_ID.add(path);
    }


    private static final String FD_NAMESPACE = FarmersDelight.MODID;

    private static final Ingredient OTHER_BLOCKING_TARGET = Ingredient.of(
            ModItems.HORSE_FEED.get(),
            ModItems.DOG_FOOD.get(),
            ModItems.SKILLET.get(),
            ModItems.STOVE.get(),
            ModItems.HONEY_GLAZED_HAM_BLOCK.get(),
            ModItems.ROAST_CHICKEN_BLOCK.get(),
            ModItems.STUFFED_PUMPKIN_BLOCK.get(),
            ModItems.SHEPHERDS_PIE_BLOCK.get(),
            ModItems.COOKING_POT.get()
    );

    private FDRecipeBlocker() {}

    /**
     * @param id      配方 id
     * @param recipe  配方本體（Recipe<?>）
     * @param lookups 來自 RecipeManager 的 HolderLookup.Provider（新版）
     * @return true 代表要擋掉
     */
    public static boolean shouldBlock(ResourceLocation id, Recipe<?> recipe, HolderLookup.Provider lookups) {
        // 1) 只處理 FD 命名空間
        if (!FD_NAMESPACE.equals(id.getNamespace())) return false;

        // 2) 只處理 crafting / cutting / cooking 三類
        if (!isTargetType(recipe)) return false;

        // 3) 取成品並判斷是否「食物」或落在其他黑名單
        final RegistryAccess ra = (lookups instanceof RegistryAccess reg) ? reg : RegistryAccess.EMPTY;
        ItemStack result = recipe.getResultItem(ra);

        if (isFood(result)) return true;
        if(OTHER_BLOCKING_ID.contains( id.getPath())) return true;
        return OTHER_BLOCKING_TARGET.test(result);
    }

    private static boolean isTargetType(Recipe<?> recipe) {
        RecipeType<?> type = recipe.getType();

        // vanilla crafting
        if (type == RecipeType.CRAFTING || recipe instanceof CraftingRecipe) return true;

        // FD cutting
        if (type == ModRecipeTypes.CUTTING.get() || recipe instanceof CuttingBoardRecipe) return true;

        // FD cooking pot
        if (type == ModRecipeTypes.COOKING.get() || recipe instanceof CookingPotRecipe) return true;

        return false;
    }

    private static boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.getComponents().has(DataComponents.FOOD);
    }
}

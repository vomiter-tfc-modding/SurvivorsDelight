package com.vomiter.survivorsdelight.adapter.cooking_pot;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CookingPotNutritionContext {
    private final List<ItemStack> inputStacks;
    private final int foodIngredientCount;
    private final Map<Item, Integer> itemCounts;

    private final ItemStack outputPreview;

    @Nullable
    private final CookingPotRecipe recipe;

    @Nullable
    private final ResourceLocation recipeKey;

    @Nullable
    private final Fluid fluid;

    private CookingPotNutritionContext(
            List<ItemStack> inputStacks,
            int foodIngredientCount,
            ItemStack outputPreview,
            @Nullable CookingPotRecipe recipe,
            @Nullable ResourceLocation recipeKey,
            @Nullable Fluid fluid
    ) {
        this.inputStacks = List.copyOf(inputStacks);
        this.foodIngredientCount = foodIngredientCount;
        this.itemCounts = buildItemCounts(inputStacks);

        this.outputPreview = outputPreview.copy();
        this.recipe = recipe;
        this.recipeKey = recipeKey;
        this.fluid = fluid;
    }

    public static CookingPotNutritionContext of(
            List<ItemStack> inputStacks,
            int foodIngredientCount,
            ItemStack outputPreview,
            @Nullable CookingPotRecipe recipe,
            @Nullable ResourceLocation recipeKey,
            @Nullable Fluid fluid
    ) {
        return new CookingPotNutritionContext(
                inputStacks,
                foodIngredientCount,
                outputPreview,
                recipe,
                recipeKey,
                fluid
        );
    }

    public List<ItemStack> inputStacks() {
        return inputStacks;
    }

    public int foodIngredientCount() {
        return foodIngredientCount;
    }

    public ItemStack outputPreview() {
        return outputPreview.copy();
    }

    @Nullable
    public CookingPotRecipe recipe() {
        return recipe;
    }

    @Nullable
    public ResourceLocation recipeKey() {
        return recipeKey;
    }

    public boolean hasRecipeKey() {
        return recipeKey != null;
    }

    public boolean recipeKeyIs(ResourceLocation id) {
        return recipeKey != null && recipeKey.equals(id);
    }

    @Nullable
    public Fluid fluid() {
        return fluid;
    }

    public boolean hasFluid() {
        return fluid != null;
    }

    public boolean fluidIs(Fluid fluid) {
        return this.fluid == fluid;
    }

    public boolean fluidIs(TagKey<Fluid> tag) {
        return fluid != null && fluid.defaultFluidState().is(tag);
    }

    public boolean contains(Item item) {
        return itemCounts.containsKey(item);
    }

    public int count(Item item) {
        return itemCounts.getOrDefault(item, 0);
    }

    public boolean contains(TagKey<Item> tag) {
        for (ItemStack stack : inputStacks) {
            if (stack.is(tag)) {
                return true;
            }
        }
        return false;
    }

    public int count(TagKey<Item> tag) {
        int total = 0;
        for (ItemStack stack : inputStacks) {
            if (stack.is(tag)) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static Map<Item, Integer> buildItemCounts(List<ItemStack> inputStacks) {
        Map<Item, Integer> counts = new HashMap<>();
        for (ItemStack stack : inputStacks) {
            counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
        }
        return counts;
    }

    @Override
    public String toString() {
        return "CookingPotNutritionContext{" +
                "inputStacks=" + inputStacks.size() +
                ", foodIngredientCount=" + foodIngredientCount +
                ", outputPreview=" + outputPreview +
                ", recipe=" + (recipe == null ? "null" : recipe.getClass().getName()) +
                ", recipeKey=" + recipeKey +
                ", fluid=" + (fluid == null ? "null" : Objects.toString(fluid)) +
                '}';
    }
}
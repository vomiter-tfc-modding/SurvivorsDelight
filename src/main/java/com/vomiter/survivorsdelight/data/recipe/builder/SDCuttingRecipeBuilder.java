package com.vomiter.survivorsdelight.data.recipe.builder;

import com.vomiter.survivorsdelight.registry.recipe.SDCuttingRecipe;
import net.dries007.tfc.common.recipes.ingredients.AndIngredient;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class SDCuttingRecipeBuilder {
    private Ingredient ingredient;
    private Ingredient tool;
    private final List<SDCuttingRecipe.Output> outputs = new ArrayList<>(); // 修改為 Output 介面
    private String group;
    private Optional<SoundEvent> sound = Optional.empty();

    public static SDCuttingRecipeBuilder cutting(Ingredient ingredient) {
        SDCuttingRecipeBuilder builder = new SDCuttingRecipeBuilder();
        builder.ingredient = ingredient;
        return builder;
    }

    public static SDCuttingRecipeBuilder cuttingNotRotten(Ingredient ingredient) {
        return cutting(AndIngredient.of(ingredient, NotRottenIngredient.INSTANCE));
    }

    public SDCuttingRecipeBuilder tool(Ingredient tool) {
        this.tool = tool;
        return this;
    }

    // --- StackOutput 相關方法 ---

    public SDCuttingRecipeBuilder addResult(Item item) {
        return addResult(item, 1, 1.0f, List.of());
    }

    public SDCuttingRecipeBuilder addResult(Item item, int count) {
        return addResult(item, count, 1.0f, List.of());
    }

    public SDCuttingRecipeBuilder addResultWithChance(Item item, float chance) {
        return addResult(item, 1, chance, List.of());
    }

    public SDCuttingRecipeBuilder addResult(Item item, int count, List<ResourceLocation> modifiers) {
        return addResult(item, count, 1.0f, modifiers);
    }

    public SDCuttingRecipeBuilder addResult(Item item, int count, float chance, ResourceLocation... modifiers) {
        return addResult(item, count, chance, List.of(modifiers));
    }

    public SDCuttingRecipeBuilder addResult(Item item, int count, float chance, List<ResourceLocation> modifiers) {
        outputs.add(new SDCuttingRecipe.StackOutput(new ItemStack(item, count), modifiers, chance));
        return this;
    }

    // --- ProviderOutput 相關方法 ---

    public SDCuttingRecipeBuilder addResult(ItemStackProvider provider) {
        outputs.add(new SDCuttingRecipe.ProviderOutput(provider));
        return this;
    }


    public SDCuttingRecipeBuilder group(String g) {
        this.group = g;
        return this;
    }

    public SDCuttingRecipeBuilder sound(SoundEvent s) {
        this.sound = Optional.of(s);
        return this;
    }

    public void save(RecipeOutput out, ResourceLocation id) {
        save(out, id, null);
    }

    public void save(RecipeOutput out, ResourceLocation id, @Nullable AdvancementHolder adv, ICondition... conditions) {
        Objects.requireNonNull(ingredient, "ingredient is required for recipe: " + id);
        Objects.requireNonNull(tool, "tool is required for recipe: " + id);
        if (outputs.isEmpty()) {
            throw new IllegalStateException("at least one result is required for recipe: " + id);
        }

        final SDCuttingRecipe recipe = new SDCuttingRecipe(
            group == null ? "" : group,
            ingredient,
            tool,
            outputs,
            sound
        );

        out.accept(id, recipe, adv, conditions);
    }
}

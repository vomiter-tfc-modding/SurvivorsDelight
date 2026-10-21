package com.vomiter.survivorsdelight.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

/** All ingredient requirements are independent predicates over TFC's stored meal components. */
public record MealModelRule(ResourceLocation id, int priority, Ingredient meal,
                            List<Requirement> ingredients, boolean exact, ResourceLocation model) {
    public MealModelRule {
        ingredients = List.copyOf(ingredients);
    }

    public static MealModelRule parse(ResourceLocation id, JsonObject json) {
        if (!json.has("meal")) throw new JsonParseException("Missing meal ingredient selector");
        Ingredient meal = Ingredient.CODEC.parse(JsonOps.INSTANCE, json.get("meal")).getOrThrow();
        List<Requirement> requirements = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(json, "ingredients");
        for (JsonElement element : array) {
            JsonObject requirement = GsonHelper.convertToJsonObject(element, "ingredient requirement");
            if (!requirement.has("ingredient")) throw new JsonParseException("Missing ingredient selector");
            int min = GsonHelper.getAsInt(requirement, "min", 1);
            int max = GsonHelper.getAsInt(requirement, "max", Integer.MAX_VALUE);
            if (min < 0 || max < min) throw new JsonParseException("Require 0 <= min <= max");
            String mode = GsonHelper.getAsString(requirement, "count_mode", "items");
            if (!mode.equals("items") && !mode.equals("stacks")) throw new JsonParseException("count_mode must be items or stacks");
            requirements.add(new Requirement(Ingredient.CODEC.parse(JsonOps.INSTANCE, requirement.get("ingredient")).getOrThrow(), min, max, mode.equals("stacks")));
        }
        return new MealModelRule(id, GsonHelper.getAsInt(json, "priority", 0), meal, requirements,
                GsonHelper.getAsBoolean(json, "exact", false),
                ResourceLocation.parse(GsonHelper.getAsString(json, "model")));
    }

    public boolean matches(ItemStack stack, List<ItemStack> components) {
        if (!test(meal, stack)) return false;
        for (Requirement requirement : ingredients) {
            long count = 0;
            for (ItemStack component : components) {
                if (!component.isEmpty() && test(requirement.ingredient(), component)) {
                    count += requirement.countStacks() ? 1 : component.getCount();
                }
            }
            if (count < requirement.min() || count > requirement.max()) return false;
        }
        if (exact) {
            for (ItemStack component : components) {
                if (!component.isEmpty() && ingredients.stream().noneMatch(r -> test(r.ingredient(), component))) return false;
            }
        }
        return true;
    }

    private static boolean test(Ingredient ingredient, ItemStack stack) {
        return ingredient.test(stack);
    }

    public record Requirement(Ingredient ingredient, int min, int max, boolean countStacks) {}
}

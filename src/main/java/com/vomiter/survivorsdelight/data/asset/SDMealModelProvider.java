package com.vomiter.survivorsdelight.data.asset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Built-in client rules use the same resource-pack format as user-defined models. */
public final class SDMealModelProvider implements DataProvider {
    private final PackOutput output;

    public SDMealModelProvider(PackOutput output) { this.output = output; }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        JsonElement sandwich = json(Ingredient.of(TFCItems.FOOD.get(Food.WHEAT_BREAD_SANDWICH).get()));
        JsonElement cabbage = json(Ingredient.of(TFCItems.FOOD.get(Food.CABBAGE).get(), ModItems.CABBAGE_LEAF.get()));
        JsonElement tomato = json(Ingredient.of(TFCItems.FOOD.get(Food.TOMATO).get()));

        save(cache, writes, "egg_sandwich", rule(sandwich, 30, BuiltInRegistries.ITEM.getKey(ModItems.EGG_SANDWICH.get()),
                requirement(json(Ingredient.of(Tags.Items.EGGS)), 2, null)));
        save(cache, writes, "chicken_sandwich", rule(sandwich, 20, BuiltInRegistries.ITEM.getKey(ModItems.CHICKEN_SANDWICH.get()),
                requirement(json(Ingredient.of(TFCItems.FOOD.get(Food.COOKED_CHICKEN).get(), ModItems.COOKED_CHICKEN_CUTS.get())), 1, 1),
                requirement(cabbage, 1, 1), requirement(json(Ingredient.of(TFCItems.FOOD.get(Food.CARROT).get())), 1, 1)));
        save(cache, writes, "bacon_sandwich", rule(sandwich, 10, BuiltInRegistries.ITEM.getKey(ModItems.BACON_SANDWICH.get()),
                requirement(json(Ingredient.of(ModItems.COOKED_BACON.get())), 1, 1),
                requirement(tomato, 1, 1), requirement(cabbage, 1, 1)));

        save(cache, writes, "mixed_salad", rule(json(Ingredient.of(TFCItems.SALADS.get(Nutrient.VEGETABLES).get())),
                0, BuiltInRegistries.ITEM.getKey(ModItems.MIXED_SALAD.get()), requirement(cabbage, 1, null), requirement(tomato, 1, null),
                requirement(json(Ingredient.of(TFCItems.FOOD.get(Food.BEET).get())), 1, null)));
        save(cache, writes, "fruit_salad", rule(json(Ingredient.of(TFCItems.SALADS.get(Nutrient.FRUIT).get())),
                0, BuiltInRegistries.ITEM.getKey(ModItems.FRUIT_SALAD.get()), requirement(json(Ingredient.of(SDTags.ItemTags.APPLE_FOR_CIDER)), 1, null),
                requirement(json(Ingredient.of(TFCItems.FOOD.get(Food.MELON_SLICE).get())), 1, null),
                requirement(json(Ingredient.of(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)), 1, null),
                requirement(json(Ingredient.of(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get())), 1, null)));

        JsonArray soups = new JsonArray();
        for (Nutrient nutrient : Nutrient.VALUES) soups.add(json(Ingredient.of(TFCItems.SOUPS.get(nutrient).get())));
        save(cache, writes, "pumpkin_soup", rule(soups, 0, BuiltInRegistries.ITEM.getKey(ModItems.PUMPKIN_SOUP.get()),
                requirement(json(Ingredient.of(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get())), 3, null)));

        JsonObject fallback = new JsonObject();
        fallback.addProperty("parent", "minecraft:item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", "tfc:item/food/wheat_bread_sandwich");
        fallback.add("textures", textures);
        writes.add(DataProvider.saveStable(cache, fallback, output.getOutputFolder().resolve(
                "assets/tfc/models/item/food/wheat_bread_sandwich.json")));
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    private void save(CachedOutput cache, List<CompletableFuture<?>> writes, String name, JsonObject rule) {
        writes.add(DataProvider.saveStable(cache, rule, output.getOutputFolder().resolve(
                "assets/" + SurvivorsDelight.MODID + "/meal_models/" + name + ".json")));
    }

    private static JsonObject rule(JsonElement meal, int priority, ResourceLocation item, JsonObject... requirements) {
        JsonObject json = new JsonObject();
        json.add("meal", meal.deepCopy());
        json.addProperty("priority", priority);
        json.addProperty("model", item.getNamespace() + ":item/" + item.getPath());
        JsonArray ingredients = new JsonArray();
        for (JsonObject requirement : requirements) ingredients.add(requirement);
        json.add("ingredients", ingredients);
        return json;
    }

    private static JsonObject requirement(JsonElement ingredient, int min, Integer max) {
        JsonObject json = new JsonObject();
        json.add("ingredient", ingredient.deepCopy());
        json.addProperty("min", min);
        if (max != null) json.addProperty("max", max);
        return json;
    }

    private static JsonElement json(Ingredient ingredient) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
    }

    @Override
    public String getName() { return "Survivor's Delight ingredient-based meal models"; }
}

package com.vomiter.survivorsdelight.client.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Client assets, deliberately reloaded before model dependencies are collected. */
public final class MealModelManager {
    public static final MealModelManager INSTANCE = new MealModelManager();
    public static final String DIRECTORY = "meal_models";
    private volatile List<MealModelRule> rules = List.of();

    private MealModelManager() {}

    public List<MealModelRule> getRules() { return rules; }

    public void reload(ResourceManager resourceManager) {
        List<MealModelRule> loaded = new ArrayList<>();
        // listResources selects the highest resource-pack version of each file.
        for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources(
                DIRECTORY, id -> id.getPath().endsWith(".json")).entrySet()) {
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonObject json = GsonHelper.convertToJsonObject(JsonParser.parseReader(reader), "meal model rule");
                if (GsonHelper.getAsBoolean(json, "enabled", true)) {
                    MealModelRule rule = MealModelRule.parse(entry.getKey(), json);
                    ResourceLocation modelFile = ResourceLocation.fromNamespaceAndPath(rule.model().getNamespace(),
                            "models/" + rule.model().getPath() + ".json");
                    if (resourceManager.getResource(modelFile).isEmpty()) {
                        SurvivorsDelight.LOGGER.error("Meal model rule {} from resource pack {} references missing file {}",
                                entry.getKey(), entry.getValue().sourcePackId(), modelFile);
                    } else {
                        loaded.add(rule);
                    }
                }
            } catch (IOException | JsonParseException | IllegalArgumentException | ResourceLocationException exception) {
                SurvivorsDelight.LOGGER.error("Invalid meal model rule {} from resource pack {}",
                        entry.getKey(), entry.getValue().sourcePackId(), exception);
            }
        }
        loaded.sort(Comparator.comparingInt(MealModelRule::priority).reversed()
                .thenComparing(rule -> rule.id().toString()));
        rules = List.copyOf(loaded);
        SurvivorsDelight.LOGGER.info("Loaded {} ingredient-based meal model rules", rules.size());
    }
}

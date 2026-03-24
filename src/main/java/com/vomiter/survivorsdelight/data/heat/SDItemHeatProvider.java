package com.vomiter.survivorsdelight.data.heat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 輸出：data/<modid>/tfc/item_heat/<namespace>/<path>.json
 *
 * 對應 TFC 1.21.1：net.dries007.tfc.common.component.heat.HeatDefinition
 * JSON keys:
 * - ingredient (required)
 * - heat_capacity (required)
 * - forging_temperature (optional, default 0)
 * - welding_temperature (optional, default 0)
 */
public class SDItemHeatProvider implements DataProvider
{
    private static final String FOLDER = "tfc/item_heat";

    private final PackOutput output;
    private final String modid;
    private final Map<ResourceLocation, JsonObject> entries = new LinkedHashMap<>();

    public SDItemHeatProvider(PackOutput output, String modid)
    {
        this.output = output;
        this.modid = modid;
        new SDHeatData(this).save();
    }

    public Builder newEntry(ResourceLocation id)
    {
        return new Builder(id);
    }

    public void putRaw(ResourceLocation id, JsonObject json)
    {
        entries.put(id, json);
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput)
    {
        final PackOutput.PathProvider pathProvider =
                output.createPathProvider(PackOutput.Target.DATA_PACK, FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonObject> e : entries.entrySet())
        {
            Path path = pathProvider.json(e.getKey());
            futures.add(DataProvider.saveStable(cachedOutput, e.getValue(), path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName()
    {
        return "TFC Item Heat Data: " + modid;
    }

    // ---------- Builder ----------

    public class Builder
    {
        private final ResourceLocation id;

        private JsonElement ingredientJson; // required

        private Float heatCapacity;          // required
        private Float forgingTemperature;    // optional (omit if 0 or null)
        private Float weldingTemperature;    // optional (omit if 0 or null)

        private final List<Consumer<JsonObject>> extras = new ArrayList<>();

        private Builder(ResourceLocation id) { this.id = id; }

        // ---- ingredient ----
        public Builder ingredient(Ingredient ing)
        {
            this.ingredientJson = SDUtils.ingredientToJsonElement(ing);
            return this;
        }
        public Builder ingredient(ItemLike item) { return ingredient(Ingredient.of(item)); }
        public Builder ingredient(ItemLike... items) { return ingredient(Ingredient.of(items)); }
        public Builder ingredient(ItemStack stack) { return ingredient(Ingredient.of(stack)); }
        public Builder ingredient(TagKey<Item> tag) { return ingredient(Ingredient.of(tag)); }
        public Builder ingredientJson(JsonElement json) { this.ingredientJson = json; return this; }

        // ---- heat fields ----
        public Builder heatCapacity(float value)
        {
            this.heatCapacity = value;
            return this;
        }

        public Builder forgingTemperature(float value)
        {
            this.forgingTemperature = value;
            return this;
        }

        public Builder weldingTemperature(float value)
        {
            this.weldingTemperature = value;
            return this;
        }

        /**
         * convenience: 一次設完
         */
        public Builder heat(float heatCapacity, float forgingTemp, float weldingTemp)
        {
            this.heatCapacity = heatCapacity;
            this.forgingTemperature = forgingTemp;
            this.weldingTemperature = weldingTemp;
            return this;
        }

        /**
         * 讓你可以塞自訂欄位（如果你之後要擴充自家讀取器）
         */
        public Builder extra(Consumer<JsonObject> extra)
        {
            this.extras.add(extra);
            return this;
        }

        public void save()
        {
            entries.put(id, build());
        }

        private JsonObject build()
        {
            JsonObject json = new JsonObject();

            if (ingredientJson == null)
                throw new IllegalStateException("Missing required 'ingredient' for " + id);
            json.add("ingredient", ingredientJson);

            if (heatCapacity == null)
                throw new IllegalStateException("Missing required 'heat_capacity' for " + id);
            json.addProperty("heat_capacity", heatCapacity);

            // TFC CODEC 是 optionalFieldOf(..., 0f)
            // 實務上：0 或 null 就不輸出，保持 JSON 精簡
            if (forgingTemperature != null && forgingTemperature.floatValue() != 0f)
                json.addProperty("forging_temperature", forgingTemperature);

            if (weldingTemperature != null && weldingTemperature.floatValue() != 0f)
                json.addProperty("welding_temperature", weldingTemperature);

            for (Consumer<JsonObject> extra : extras)
                extra.accept(json);

            return json;
        }
    }

    public ResourceLocation id(String path)
    {
        return SDUtils.RLUtils.build(modid, path);
    }
}
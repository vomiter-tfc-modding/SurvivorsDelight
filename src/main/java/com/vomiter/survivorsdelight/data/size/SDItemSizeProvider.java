package com.vomiter.survivorsdelight.data.size;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
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
 * 輸出：data/<modid>/tfc/item_sizes/<namespace>/<path>.json
 */
public class SDItemSizeProvider implements DataProvider {
    private static final String FOLDER = "tfc/item_size";

    private final PackOutput output;
    private final String modid;
    private final Map<ResourceLocation, JsonObject> entries = new LinkedHashMap<>();

    public SDItemSizeProvider(PackOutput output, String modid) {
        this.output = output;
        this.modid = modid;
        new FDSizeData(this).save();
    }

    public Builder newEntry(ResourceLocation itemId) {
        return new Builder(itemId);
    }

    public void putRaw(ResourceLocation itemId, JsonObject json) {
        entries.put(itemId, json);
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        final PackOutput.PathProvider pathProvider =
                output.createPathProvider(PackOutput.Target.DATA_PACK, FOLDER);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonObject> e : entries.entrySet()) {
            Path path = pathProvider.json(e.getKey());
            futures.add(DataProvider.saveStable(cachedOutput, e.getValue(), path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "TFC Item Size Data: " + modid;
    }

    // ---------- Builder ----------

    public class Builder {
        private final ResourceLocation id;
        private JsonElement ingredientJson;     // 必填
        private Size size;
        private Weight weight;
        private Integer maxStackSize; // 可選：若你想一併寫入（鍵名可在 build() 改）
        private final List<Consumer<JsonObject>> extras = new ArrayList<>();

        private Builder(ResourceLocation id) { this.id = id; }

        public Builder ingredient(Ingredient ing){
            this.ingredientJson = SDUtils.ingredientToJsonElement(ing); return this;
        }
        public Builder ingredient(ItemLike item){
            return ingredient(Ingredient.of(item));
        }
        public Builder ingredient(ItemLike... items){
            return ingredient(Ingredient.of(items));
        }
        public Builder ingredient(ItemStack stack){
            return ingredient(Ingredient.of(stack));
        }
        public Builder ingredient(TagKey<Item> tag){
            return ingredient(Ingredient.of(tag));
        }
        public Builder ingredientJson(JsonElement json){
            this.ingredientJson = json; return this;
        }

        public Builder size(Size size) {
            this.size = size; return this;
        }

        public Builder weight(Weight weight) {
            this.weight = weight; return this;
        }

        public void save() {
            entries.put(id, build());
        }

        private JsonObject build() {
            JsonObject json = new JsonObject();
            if (ingredientJson == null) {
                throw new IllegalStateException("Missing required 'ingredient' for " + id);
            }
            json.add("ingredient", ingredientJson);

            if (size != null) {
                json.addProperty("size", size.name);
            }
            if (weight != null) {
                json.addProperty("weight", weight.name);
            }
            return json;
        }
    }

    public ResourceLocation id(String path) {
        return SDUtils.RLUtils.build(modid, path);
    }
}

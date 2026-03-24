package com.vomiter.survivorsdelight.data.book.builder;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class EntryJson {
    private final String id;
    private final Map<String, JsonElement> map;
    private final List<JsonObject> pages;
    private final Map<String, Integer> extraMapping;

    public static String id(String s){
        return SurvivorsDelight.MODID + "/" + s;
    }

    public String getId(){
        return id;
    }

    private EntryJson(String id, Map<String, JsonElement> map, List<JsonObject> pages, @Nullable Map<String, Integer> extraMapping) {
        this.id = id(id); this.map = map; this.pages = pages; this.extraMapping = extraMapping;
    }

    public JsonObject toJson() {
        JsonObject obj = BookJson.mapToObj(map);
        JsonArray arr = new JsonArray();
        for (JsonObject p : pages) arr.add(p);
        obj.add("pages", arr);
        if(extraMapping != null){
            JsonObject obj2 = new JsonObject();
            extraMapping.forEach(obj2::addProperty);
            obj.add("extra_recipe_mappings", obj2);
        }
        return obj;
    }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private final Map<String, JsonElement> m = Maps.newLinkedHashMap();
        private final List<JsonObject> pages = new ArrayList<>();
        private final Map<String, Integer> extraMapping = new LinkedHashMap<>();
        public Builder(String id) { this.id = id; }
        public Builder setName(String name) { m.put("name", new JsonPrimitive(name)); return this; }
        public Builder setCategory(String rl) { m.put("category", new JsonPrimitive(rl)); return this; }
        public Builder setIcon(ResourceLocation itemId){ return setIcon(itemId.toString());}
        public Builder setIcon(String itemId) { m.put("icon", new JsonPrimitive(itemId)); return this; }
        public Builder setIcon(Supplier<Item> item){
            return setIcon(BuiltInRegistries.ITEM.getKey(item.get()));
        }
        public Builder setReadByDefault(boolean b) { m.put("read_by_default", new JsonPrimitive(b)); return this; }
        public Builder setSortnum(int n) { m.put("sortnum", new JsonPrimitive(n)); return this; }
        public Builder addTextPage(String text) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:text");
            p.addProperty("text", text);
            pages.add(p); return this;
        }
        public Builder addTextPage(String title, String anchor, String text) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:text");
            p.addProperty("text", text);
            p.addProperty("title", title);
            p.addProperty("anchor", anchor);
            pages.add(p); return this;
        }

        public Builder addImagePage(String[] images, String title) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:image");
            JsonArray arr = new JsonArray();
            for (String s : images) arr.add(new JsonPrimitive(s));
            p.add("images", arr);
            if (title != null && !title.isEmpty()) p.addProperty("title", title);
            pages.add(p); return this;
        }

        public Builder addSpotlightPage(String item, String text) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:spotlight");
            p.addProperty("item", item);
            if (text != null) p.addProperty("text", text);
            pages.add(p); return this;
        }

        public Builder addSpotlightPage(JsonElement json, String text) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:spotlight");
            p.add("item", json);
            if (text != null) p.addProperty("text", text);
            pages.add(p); return this;
        }


        public Builder addSmeltingPage(String recipeRL, String text) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:smelting");
            p.addProperty("recipe", recipeRL);
            if (text != null) p.addProperty("text", text);
            pages.add(p); return this;
        }
        public Builder addLinkPage(String url, String text) {
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:link");
            p.addProperty("url", url);
            if (text != null) p.addProperty("text", text);
            pages.add(p); return this;
        }

        public Builder addSingleBlockPage(String title, String block){
            JsonArray pattern = new JsonArray();
            JsonArray r1 = new JsonArray(); r1.add("X");
            JsonArray r2 = new JsonArray(); r2.add("0");
            pattern.add(r1);
            pattern.add(r2);
            JsonObject mapping = new JsonObject();
            mapping.addProperty("X", block);
            JsonObject multiblock = new JsonObject();
            multiblock.add("pattern", pattern);
            multiblock.add("mapping", mapping);

            JsonObject page = new JsonObject();
            page.addProperty("type", "patchouli:multiblock");
            page.add("multiblock", multiblock);
            page.addProperty("name", title);
            page.addProperty("text", "");
            page.addProperty("enable_visualize", false);

            pages.add(page);
            return this;
        }

        public Builder addAnvilRecipe(ResourceLocation recipeRL, String text){
            JsonObject p = new JsonObject();
            p.addProperty("type", "tfc:anvil_recipe");
            p.addProperty("recipe", recipeRL.toString());
            if (text != null) p.addProperty("text", text);
            pages.add(p); return this;
        }

        public Builder addWeldingRecipe(ResourceLocation recipeRL, String text){
            JsonObject p = new JsonObject();
            p.addProperty("type", "tfc:welding_recipe");
            p.addProperty("recipe", recipeRL.toString());
            if (text != null) p.addProperty("text", text);
            pages.add(p); return this;
        }

        public Builder addCraftingRecipe(ResourceLocation recipeRL, ResourceLocation recipeRL2, String title){
            JsonObject p = new JsonObject();
            p.addProperty("type", "patchouli:crafting");
            p.addProperty("recipe", recipeRL.toString());
            if(recipeRL2 != null) p.addProperty("recipe2", recipeRL2.toString());
            if (title != null) p.addProperty("title", title);
            pages.add(p); return this;
        }

        public Builder addCraftingRecipe(ResourceLocation recipeRL, String title){
            return addCraftingRecipe(recipeRL, null, title);
        }

        public Builder extraRecipeMapping(Supplier<Item> item, int page){
            extraMapping.put(BuiltInRegistries.ITEM.getKey(item.get()).toString(),page);
            return this;
        }

        public Builder extraRecipeMapping(TagKey<Item> tagKey, int page){
            extraMapping.put("tag:" + tagKey.location(), page);
            return this;
        }


        public EntryJson build() { return new EntryJson(id, m, pages, extraMapping); }
    }
}

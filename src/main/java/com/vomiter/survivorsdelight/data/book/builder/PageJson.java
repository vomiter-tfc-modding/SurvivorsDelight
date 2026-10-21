package com.vomiter.survivorsdelight.data.book.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/** Reusable Patchouli page definitions. Each build and insertion takes a snapshot. */
public final class PageJson {
    private final JsonObject json;

    private PageJson(JsonObject json) { this.json = json.deepCopy(); }
    public JsonObject toJson() { return json.deepCopy(); }

    /** Escape hatch for Patchouli pages and templates supplied by other mods. */
    public static Builder builder(String type) { return new Builder(type); }
    public static Builder text(String text) {
        Builder page = builder("patchouli:text");
        page.json.addProperty("text", text);
        return page;
    }
    public static Builder text(String title, String anchor, String text) {
        Builder page = text(text);
        page.json.addProperty("title", title);
        page.json.addProperty("anchor", anchor);
        return page;
    }
    public static Builder spotlight(String item, String text) {
        return builder("patchouli:spotlight").setString("item", item).setText(text);
    }
    public static Builder spotlight(JsonElement item, String text) {
        return builder("patchouli:spotlight").setJson("item", item).setText(text);
    }
    public static Builder image(String[] images, String title) {
        JsonArray array = new JsonArray();
        for (String image : images) array.add(image);
        Builder page = builder("patchouli:image").setJson("images", array);
        if (title != null && !title.isEmpty()) page.setTitle(title);
        return page;
    }
    public static Builder link(String url, String text) {
        return builder("patchouli:link").setString("url", url).setText(text);
    }
    public static Builder recipe(String type, ResourceLocation recipe, String text) {
        return builder(type).setString("recipe", recipe.toString()).setText(text);
    }
    public static Builder heating(ResourceLocation recipe, String text) {
        return recipe("tfc:heat_recipe", recipe, text == null ? "" : text);
    }
    public static Builder crafting(ResourceLocation recipe, ResourceLocation recipe2, String title) {
        Builder page = recipe("patchouli:crafting", recipe, null).setTitle(title);
        if (recipe2 != null) page.setString("recipe2", recipe2.toString());
        return page;
    }
    public static Builder multiblock(String title, MultiblockJson multiblock, String text) {
        return builder("patchouli:multiblock").setString("name", title)
                .setJson("multiblock", multiblock.toJson()).setText(text).setVisualize(false);
    }
    public static Builder multiblock(String title, ResourceLocation multiblockId, String text) {
        return builder("patchouli:multiblock").setString("name", title)
                .setString("multiblock_id", multiblockId.toString()).setText(text).setVisualize(false);
    }

    public static final class Builder {
        private final JsonObject json = new JsonObject();
        private Builder(String type) { json.addProperty("type", type); }
        public Builder setTitle(String title) { return setString("title", title); }
        public Builder setAnchor(String anchor) { return setString("anchor", anchor); }
        public Builder setText(String text) { return setString("text", text); }
        public Builder setText(TextBuilder text) { return setText(text.toString()); }
        public Builder setVisualize(boolean enabled) { return setBoolean("enable_visualize", enabled); }
        public Builder setString(String key, String value) {
            if (value != null) json.addProperty(key, value);
            return this;
        }
        public Builder setBoolean(String key, boolean value) { json.addProperty(key, value); return this; }
        public Builder setNumber(String key, Number value) { json.addProperty(key, value); return this; }
        public Builder setJson(String key, JsonElement value) { json.add(key, value.deepCopy()); return this; }
        public PageJson build() { return new PageJson(json); }
    }
}

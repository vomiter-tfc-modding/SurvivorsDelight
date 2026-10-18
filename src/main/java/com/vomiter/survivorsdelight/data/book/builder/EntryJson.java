package com.vomiter.survivorsdelight.data.book.builder;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        this.id = id(id);
        this.map = new LinkedHashMap<>();
        map.forEach((key, value) -> this.map.put(key, value.deepCopy()));
        this.pages = pages.stream().map(JsonObject::deepCopy).toList();
        this.extraMapping = extraMapping == null ? null : new LinkedHashMap<>(extraMapping);
    }

    public JsonObject toJson() {
        JsonObject obj = BookJson.mapToObj(map);
        JsonArray arr = new JsonArray();
        for (JsonObject p : pages) arr.add(p.deepCopy());
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
        private final Map<String, String> anchorMappings = new LinkedHashMap<>();
        public Builder(String id) { this.id = id; }
        public Builder setName(String name) { m.put("name", new JsonPrimitive(name)); return this; }
        public Builder setCategory(String rl) { m.put("category", new JsonPrimitive(rl)); return this; }
        public Builder setIcon(ResourceLocation itemId){ return setIcon(itemId.toString());}
        public Builder setIcon(String itemId) { m.put("icon", new JsonPrimitive(itemId)); return this; }
        public Builder setIcon(RegistryObject<? extends ItemLike> item) { return setIcon(item.getId()); }
        public Builder setIcon(ItemLike item) { return setIcon(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem()))); }
        public Builder setCategory(ResourceLocation category) { return setCategory(category.toString()); }
        public Builder setReadByDefault(boolean b) { m.put("read_by_default", new JsonPrimitive(b)); return this; }
        public Builder setSortnum(int n) { m.put("sortnum", new JsonPrimitive(n)); return this; }
        public Builder addPage(PageJson page) { return addPage(page.toJson()); }
        public Builder addPage(PageJson.Builder page) { return addPage(page.build()); }
        public Builder addPage(JsonObject page) { pages.add(page.deepCopy()); return this; }
        public int pageCount() { return pages.size(); }
        public int lastPageIndex() {
            if (pages.isEmpty()) throw new IllegalStateException("No pages have been added");
            return pages.size() - 1;
        }
        /** Attach an anchor without requiring a particular page type. */
        public Builder anchor(String anchor) {
            pages.get(lastPageIndex()).addProperty("anchor", anchor);
            return this;
        }
        public Builder addTextPage(String text) { return addPage(PageJson.text(text)); }
        public Builder addText(TextBuilder text) { return addTextPage(text.toString()); }
        public Builder addTextPage(String title, String anchor, String text) {
            return addPage(PageJson.text(title, anchor, text));
        }
        public Builder addImagePage(String[] images, String title) { return addPage(PageJson.image(images, title)); }
        public Builder addSpotlightPage(String item, String text) { return addPage(PageJson.spotlight(item, text)); }
        public Builder addSpotlightPage(JsonElement item, String text) { return addPage(PageJson.spotlight(item, text)); }
        public Builder addSpotlightPage(Ingredient ingredient, String text) { return addSpotlightPage(ingredient.toJson(), text); }
        public Builder addSpotlightPage(RegistryObject<? extends ItemLike> item, String text) {
            return addSpotlightPage(item.getId().toString(), text);
        }
        public Builder addSpotlightPage(ItemLike item, String text) {
            return addSpotlightPage(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())).toString(), text);
        }
        public Builder addSmeltingPage(String recipeRL, String text) {
            return addPage(PageJson.builder("patchouli:smelting").setString("recipe", recipeRL).setText(text));
        }
        public Builder addLinkPage(String url, String text) { return addPage(PageJson.link(url, text)); }

        /** Retains the original two-layer single-block layout and disabled visualization. */
        public Builder addSingleBlockPage(String title, String block) {
            return addMultiblockPage(title, MultiblockJson.builder().layer("X").layer("0").map('X', block).build(), "");
        }
        public Builder addSingleBlockPage(String title, ResourceLocation block) { return addSingleBlockPage(title, block.toString()); }
        public Builder addSingleBlockPage(String title, RegistryObject<? extends Block> block) { return addSingleBlockPage(title, block.getId()); }
        public Builder addSingleBlockPage(String title, TagKey<Block> tag) { return addSingleBlockPage(title, "#" + tag.location()); }
        public Builder addSingleBlockPage(String title, BlockState state) {
            return addMultiblockPage(title, MultiblockJson.builder().layer("X").layer("0").map('X', state).build(), "");
        }
        public Builder addMultiblockPage(String title, MultiblockJson multiblock, String text) {
            return addPage(PageJson.multiblock(title, multiblock, text));
        }
        public Builder addMultiblockPage(String title, ResourceLocation multiblockId, String text) {
            return addPage(PageJson.multiblock(title, multiblockId, text));
        }
        /** Blocks are supplied from bottom to top, e.g. stove, cooking pot. */
        public Builder addStackedBlockPage(String title, String text, String... blocks) {
            return addMultiblockPage(title, MultiblockJson.stacked(blocks), text);
        }
        public Builder addHeatingRecipe(ResourceLocation recipeRL, String text) {
            return addPage(PageJson.heating(recipeRL, text));
        }
        public Builder addHeatingRecipe(ResourceLocation recipeRL) { return addHeatingRecipe(recipeRL, null); }
        public Builder addAnvilRecipe(ResourceLocation recipeRL, String text) {
            return addPage(PageJson.recipe("tfc:anvil_recipe", recipeRL, text));
        }
        public Builder addWeldingRecipe(ResourceLocation recipeRL, String text) {
            return addPage(PageJson.recipe("tfc:welding_recipe", recipeRL, text));
        }
        public Builder addCraftingRecipe(ResourceLocation recipeRL, ResourceLocation recipeRL2, String title) {
            return addPage(PageJson.crafting(recipeRL, recipeRL2, title));
        }
        public Builder addCraftingRecipe(ResourceLocation recipeRL, String title) {
            return addCraftingRecipe(recipeRL, null, title);
        }

        public Builder extraRecipeMapping(RegistryObject<? extends ItemLike> item, int page){
            return extraRecipeMapping(item.getId(), page);
        }

        public Builder extraRecipeMapping(TagKey<Item> tagKey, int page){
            String key = "tag:" + tagKey.location();
            extraMapping.put(key, page);
            anchorMappings.remove(key);
            return this;
        }


        public Builder extraRecipeMapping(ResourceLocation item, int page) {
            extraMapping.put(item.toString(), page);
            anchorMappings.remove(item.toString());
            return this;
        }
        public Builder extraRecipeMapping(ItemLike item, int page) {
            return extraRecipeMapping(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem())), page);
        }
        /** Map to the most recently added page, avoiding manually counted indices. */
        public Builder extraRecipeMapping(RegistryObject<? extends ItemLike> item) {
            return extraRecipeMapping(item, lastPageIndex());
        }
        public Builder extraRecipeMapping(TagKey<Item> tag) { return extraRecipeMapping(tag, lastPageIndex()); }
        public Builder extraRecipeMapping(ResourceLocation item) { return extraRecipeMapping(item, lastPageIndex()); }
        public Builder extraRecipeMapping(RegistryObject<? extends ItemLike> item, String anchor) {
            return extraRecipeMapping(item.getId(), anchor);
        }
        public Builder extraRecipeMapping(ResourceLocation item, String anchor) {
            anchorMappings.put(item.toString(), anchor);
            extraMapping.remove(item.toString());
            return this;
        }
        public Builder extraRecipeMapping(TagKey<Item> tag, String anchor) {
            String key = "tag:" + tag.location();
            anchorMappings.put(key, anchor);
            extraMapping.remove(key);
            return this;
        }
        public EntryJson build() {
            Map<String, Integer> resolved = new LinkedHashMap<>(extraMapping);
            anchorMappings.forEach((item, anchor) -> {
                int found = -1;
                for (int i = 0; i < pages.size(); i++) {
                    JsonElement value = pages.get(i).get("anchor");
                    if (value != null && !value.isJsonNull() && value.getAsString().equals(anchor)) {
                        if (found != -1) throw new IllegalStateException("Duplicate page anchor: " + anchor);
                        found = i;
                    }
                }
                if (found == -1) throw new IllegalStateException("Unknown page anchor: " + anchor);
                resolved.put(item, found);
            });
            return new EntryJson(id, m, pages, resolved);
        }
    }
}

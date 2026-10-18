package com.vomiter.survivorsdelight.data.book.builder;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;


public final class CategoryJson {
    private final String id;
    private final Map<String, JsonElement> map;

    private CategoryJson(String id, Map<String, JsonElement> map) {
        this.id = id; this.map = new LinkedHashMap<>(map);
    }

    public String id() { return id; }
    public JsonObject toJson() { return BookJson.mapToObj(map); }

    public static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private final Map<String, JsonElement> m = Maps.newLinkedHashMap();
        Builder(String id) { this.id = id; }
        public Builder setName(String name) { m.put("name", new JsonPrimitive(name)); return this; }
        public Builder setDescription(String desc) { m.put("description", new JsonPrimitive(desc)); return this; }
        public Builder setIcon(String itemId) { m.put("icon", new JsonPrimitive(itemId)); return this; }
        public Builder setIcon(ResourceLocation item) { return setIcon(item.toString()); }
        public Builder setIcon(RegistryObject<? extends ItemLike> item) { return setIcon(item.getId()); }
        public Builder setIcon(ItemLike item) { return setIcon(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.asItem()))); }
        public Builder setSortnum(int n) { m.put("sortnum", new JsonPrimitive(n)); return this; }
        public CategoryJson build() { return new CategoryJson(id, m); }
    }
}

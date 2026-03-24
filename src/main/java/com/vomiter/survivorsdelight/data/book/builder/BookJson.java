package com.vomiter.survivorsdelight.data.book.builder;


import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


import java.util.Map;


public final class BookJson {
    private final Map<String, JsonElement> map;
    private BookJson(Map<String, JsonElement> map) { this.map = map; }
    public JsonObject toJson() { return mapToObj(map); }
    static Builder builder() { return new Builder(); }

    static final class Builder {
        private final Map<String, JsonElement> m = Maps.newLinkedHashMap();
        Builder setName(String name) { m.put("name", new JsonPrimitive(name)); return this; }
        Builder setI18n(boolean i18n) { m.put("i18n", new JsonPrimitive(i18n)); return this; }
        Builder setSubtitle(String subtitle) { m.put("subtitle", new JsonPrimitive(subtitle)); return this; }
        Builder setLandingText(String text) { m.put("landing_text", new JsonPrimitive(text)); return this; }
        Builder setCreativeTab(String tab) { m.put("creative_tab", new JsonPrimitive(tab)); return this; }
        Builder setBookTexture(String tex) { m.put("book_texture", new JsonPrimitive(tex)); return this; }
        Builder setModel(String model) { m.put("model", new JsonPrimitive(model)); return this; }
        Builder setShowProgress(boolean b) { m.put("show_progress", new JsonPrimitive(b)); return this; }
        Builder setUseResourcePack(boolean b) { m.put("use_resource_pack", new JsonPrimitive(b)); return this; }
        Builder setVersion(int v) { m.put("version", new JsonPrimitive(v)); return this; }
        BookJson build() { return new BookJson(m); }
    }


    static JsonObject mapToObj(Map<String, JsonElement> m) {
        JsonObject o = new JsonObject();
        m.forEach(o::add);
        return o;
    }
}
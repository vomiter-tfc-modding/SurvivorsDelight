package com.vomiter.survivorsdelight.data.asset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

public final class ForeignLangReader {
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, String>>(){}.getType();

    /** 讀 assets/<modid>/lang/<locale>.json（從 datagen 類載入器或依賴 jar 中） */
    public static Map<String, String> load(String modid, String locale) {
        final String path = "assets/" + modid + "/lang/" + locale + ".json";
        final var cl = ForeignLangReader.class.getClassLoader(); // datagen 過程下可從 classpath 抓資源
        try (var in = cl.getResourceAsStream(path)) {
            if (in == null) return Collections.emptyMap();
            try (var rd = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return GSON.fromJson(rd, MAP_TYPE);
            }
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}

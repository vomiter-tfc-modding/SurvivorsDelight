package com.vomiter.survivorsdelight.data.heat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class BuiltinHeatJson {
    private BuiltinHeatJson() {}

    public record HeatTemps(float forging, float welding, float heatCapacity) {}

    /**
     * 從「JAR 內建資源」讀取 heat json（繞過 datapack / ResourceManager）。
     *
     * 例：要讀 data/tfc/tfc/item_heat/copper/ingot.json
     * 傳入：new ResourceLocation("tfc", "tfc/item_heat/copper/ingot")
     */
    public static Optional<HeatTemps> readBuiltinHeatTemps(Class<?> anchor, ResourceLocation id) {
        // 注意：ResourceLocation 的 path 不含 ".json"
        final String classpath = "data/" + id.getNamespace() + "/" + id.getPath() + ".json";

        try (InputStream in = anchor.getClassLoader().getResourceAsStream(classpath)) {
            if (in == null) return Optional.empty();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

                float forging = obj.has("forging_temperature") ? obj.get("forging_temperature").getAsFloat() : 0f;
                float welding = obj.has("welding_temperature") ? obj.get("welding_temperature").getAsFloat() : 0f;
                float cap = obj.has("heat_capacity") ? obj.get("heat_capacity").getAsFloat() : 0f;

                return Optional.of(new HeatTemps(forging, welding, cap));
            }
        } catch (Exception e) {
            // 你可以改成 logger.warn(...)
            return Optional.empty();
        }
    }
}
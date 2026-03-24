package com.vomiter.survivorsdelight.data.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class SDCabinetLootTableProvider implements DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final PackOutput output;

    public SDCabinetLootTableProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        var pathProvider = this.output.createPathProvider(
                PackOutput.Target.DATA_PACK, "loot_table/blocks"
        );

        CompletableFuture<?>[] tasks = Arrays.stream(Wood.values()).map(wood -> {
            ResourceLocation id = SDUtils.RLUtils.build(
                    SurvivorsDelight.MODID, "planks/cabinet/" + wood.getSerializedName()
            );
            Path path = pathProvider.json(id);
            JsonObject json = buildCabinetLootJson(wood.getSerializedName());
            return DataProvider.saveStable(cache, json, path);

        }).toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(tasks);
    }

    @Override
    public @NotNull String getName() {
        return "Loot Table (Cabinets): " + SurvivorsDelight.MODID;
    }

    private static JsonObject buildCabinetLootJson(String material) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");

        JsonArray pools = new JsonArray();
        JsonObject pool = new JsonObject();
        pool.addProperty("name", "pool1");
        pool.addProperty("rolls", 1);

        // entries
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", "survivorsdelight:planks/cabinet/" + material);

        entries.add(entry);
        pool.add("entries", entries);

        // conditions
        JsonArray conditions = new JsonArray();
        JsonObject cond = new JsonObject();
        cond.addProperty("condition", "minecraft:survives_explosion");
        conditions.add(cond);
        pool.add("conditions", conditions);

        pools.add(pool);
        root.add("pools", pools);

        return root;
    }
}

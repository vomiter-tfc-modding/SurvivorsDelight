package com.vomiter.survivorsdelight.data.book.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.function.Supplier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Patchouli dense pattern: layers from top to bottom, then X rows containing Z columns. */
public final class MultiblockJson {
    private final JsonObject json;
    private MultiblockJson(JsonObject json) { this.json = json.deepCopy(); }
    public JsonObject toJson() { return json.deepCopy(); }
    public static Builder builder() { return new Builder(); }

    /** Blocks are supplied from bottom to top. The bottom block is the origin. */
    public static MultiblockJson stacked(String... blocks) {
        if (blocks.length == 0) throw new IllegalArgumentException("Supply at least one block");
        Builder builder = builder();
        for (int i = blocks.length - 1; i >= 0; i--) {
            char symbol = i == 0 ? '0' : (char) ('A' + i - 1);
            if (i > 26) throw new IllegalArgumentException("Use a pattern for more than 27 blocks");
            builder.layer(String.valueOf(symbol)).map(symbol, blocks[i]);
        }
        return builder.build();
    }

    public static final class Builder {
        private final List<String[]> layers = new ArrayList<>();
        private final Map<Character, String> mapping = new LinkedHashMap<>();
        private boolean symmetrical;
        private int[] offset;

        public Builder layer(String... rows) { layers.add(rows.clone()); return this; }
        public Builder map(char symbol, String block) {
            mapping.put(symbol, Objects.requireNonNull(block));
            return this;
        }
        public Builder map(char symbol, ResourceLocation block) { return map(symbol, block.toString()); }
        public Builder map(char symbol, Supplier<? extends Block> block) { return map(symbol, Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block.get()))); }
        public Builder map(char symbol, Block block) {
            return map(symbol, Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)));
        }
        public Builder map(char symbol, BlockState state) { return map(symbol, BlockStateParser.serialize(state)); }
        public Builder map(char symbol, TagKey<Block> tag) { return map(symbol, "#" + tag.location()); }
        public Builder setSymmetrical(boolean value) { symmetrical = value; return this; }
        public Builder setOffset(int x, int y, int z) { offset = new int[]{x, y, z}; return this; }

        public MultiblockJson build() {
            if (layers.isEmpty() || layers.get(0).length == 0 || layers.get(0)[0].isEmpty()) {
                throw new IllegalArgumentException("A multiblock pattern must have nonempty dimensions");
            }
            int rows = layers.get(0).length;
            int columns = layers.get(0)[0].length();
            int origins = 0;
            JsonArray pattern = new JsonArray();
            for (String[] layer : layers) {
                if (layer.length != rows) throw new IllegalArgumentException("Multiblock layers must have equal row counts");
                JsonArray array = new JsonArray();
                for (String row : layer) {
                    if (row.length() != columns) throw new IllegalArgumentException("Multiblock rows must have equal widths");
                    for (char symbol : row.toCharArray()) {
                        if (symbol == '0') origins++;
                        if (symbol != '0' && symbol != ' ' && symbol != '_' && !mapping.containsKey(symbol)) {
                            throw new IllegalArgumentException("Unmapped multiblock symbol: " + symbol);
                        }
                    }
                    array.add(row);
                }
                pattern.add(array);
            }
            if (origins != 1) throw new IllegalArgumentException("A multiblock pattern requires exactly one '0' origin");
            JsonObject mappings = new JsonObject();
            mapping.forEach((key, value) -> mappings.addProperty(key.toString(), value));
            JsonObject json = new JsonObject();
            json.add("pattern", pattern);
            json.add("mapping", mappings);
            if (symmetrical) json.addProperty("symmetrical", true);
            if (offset != null) {
                JsonArray array = new JsonArray();
                for (int coordinate : offset) array.add(coordinate);
                json.add("offset", array);
            }
            return new MultiblockJson(json);
        }
    }
}

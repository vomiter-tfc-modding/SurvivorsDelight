package com.vomiter.survivorsdelight.data.food;

import com.google.common.hash.HashCode;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.items.Food;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SDFoodDataProvider implements DataProvider {
    private final PackOutput packOutput;
    private final String modid;
    private final String name;

    private final Map<ResourceLocation, JsonObject> entries = new LinkedHashMap<>();

    public SDFoodDataProvider(PackOutput packOutput, String modid) {
        this.packOutput = packOutput;
        this.modid = modid;
        this.name = null;
    }

    public SDFoodDataProvider(PackOutput packOutput, String modid, String name) {
        this.packOutput = packOutput;
        this.modid = modid;
        this.name = name;
    }

    public static FoodData readJson(JsonObject json) {
        return FoodData.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(msg -> {
                    System.err.println("[FoodData] parse error: " + msg);
                })
                .orElse(FoodData.EMPTY);
    }

    /** 使用單一 item 作為 ingredient（最常見情境） */
    public SDFoodDataProvider addStatic(String id, Item item,
                                        int hunger, double saturation, double water,
                                        double decayModifier,
                                        double grain, double fruit, double vegetables, double protein, double dairy) {
        JsonObject root = new JsonObject();
        root.add("ingredient", ingredientOf(item));
        root.addProperty("hunger", hunger);
        root.addProperty("saturation", saturation);
        root.addProperty("water", water);
        root.addProperty("decay_modifier", decayModifier);
        root.addProperty("grain", grain);
        root.addProperty("fruit", fruit);
        root.addProperty("vegetables", vegetables);
        root.addProperty("protein", protein);
        root.addProperty("dairy", dairy);
        entries.put(id(id), root);
        return this;
    }

    private static final Gson PRETTY = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(
                Double.class, (JsonSerializer<Double>)
                    (src, type, ctx) ->
                        new JsonPrimitive(Math.round(src * 10.0) / 10.0)
            )
            .disableHtmlEscaping()
            .create();

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Map.Entry<ResourceLocation, JsonObject> e : entries.entrySet()) {
            Path path = outputPath(e.getKey());
            String json = PRETTY.toJson(e.getValue());
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new RuntimeException(ex);
            }
            byte[] digest = md.digest(bytes);
            HashCode hash = HashCode.fromBytes(digest);

            // 包成 CompletableFuture 以維持非同步語意
            futures.add(CompletableFuture.runAsync(() -> {
                try {
                    cachedOutput.writeIfNeeded(path, bytes, hash);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }));
        }
        if (futures.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return name == null ? modid + " TFC Food Items": modid + " " + name;
    }

    // ---------- Helpers ----------

    private ResourceLocation id(String pathName) {
        return SDUtils.RLUtils.build(modid, pathName);
    }

    private Path outputPath(ResourceLocation id) {
        // data/<modid>/tfc/food/<path>.json
        return packOutput.getOutputFolder()
                .resolve("data/" + id.getNamespace() + "/tfc/food/" + id.getPath() + ".json");
    }

    private static JsonObject ingredientOf(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null) throw new IllegalArgumentException("Unregistered item: " + item);
        JsonObject obj = new JsonObject();
        obj.addProperty("item", key.toString());
        return obj;
    }

    private static JsonObject ingredientTag(String tagPath) {
        JsonObject obj = new JsonObject();
        obj.addProperty("tag", tagPath);
        return obj;
    }

    public static JsonArray ingredientAny(JsonElement... ingredients) {
        JsonArray arr = new JsonArray();
        for (JsonElement el : ingredients) arr.add(el);
        return arr;
    }

    public FoodData readTfcFoodJson(Food food) {
        return readTfcFoodJson(food.name().toLowerCase(Locale.ROOT));
    }


    public FoodData readTfcFoodJson(String path) {
        final ResourceLocation rl = SDUtils.RLUtils.build("tfc", path);
        final String cpPath = "data/" + rl.getNamespace() + "/tfc/food/" + rl.getPath() + ".json";
        InputStream in = getClass().getClassLoader().getResourceAsStream(cpPath);
        Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);
        return readJson(JsonParser.parseReader(r).getAsJsonObject());
    }

    public Builder newBuilder(String id) { return new Builder(this, id); }

    /** 方便使用：用 item 或 tag 做 ingredient，未指定營養=0，decay=1 */
    public static final class Builder {
        private final SDFoodDataProvider parent;
        private final String id;
        private Item item;

        private JsonElement ingredient; // item / tag / 自定義 JSON
        private int hunger = 0;
        private float nutrient_multipler = 1;
        private double saturation = 0.0;
        private double water = 0.0;
        private double decay = 1.0;
        private double grain = 0.0;
        private double fruit = 0.0;
        private double vegetables = 0.0;
        private double protein = 0.0;
        private double dairy = 0.0;
        private String type;

        private Builder(SDFoodDataProvider parent, String id) {
            this.parent = parent;
            this.id = id;
        }

        public Builder from(Food food) {
            return from(parent.readTfcFoodJson(food));
        }
        public Builder from(FoodData data) {
            this.hunger = data.hunger();
            this.saturation = data.saturation();
            this.water = data.water();
            this.grain = data.nutrient(Nutrient.GRAIN);
            this.fruit = data.nutrient(Nutrient.FRUIT);
            this.vegetables = data.nutrient(Nutrient.VEGETABLES);
            this.protein = data.nutrient(Nutrient.PROTEIN);
            this.dairy = data.nutrient(Nutrient.DAIRY);
            this.decay = data.decayModifier();
            return this;
        }

        public Builder slicedFrom(Food food, int n) {
            return slicedFrom(parent.readTfcFoodJson(food), n);
        }

        public Builder slicedFrom(FoodData data, int n) {
            if (n <= 0) throw new IllegalArgumentException("n must be >= 1");
            this.hunger = Math.round(data.hunger() / (float) n);
            this.saturation = data.saturation() / n;
            this.water = data.water() / n;
            this.grain = data.nutrient(Nutrient.GRAIN) / n;
            this.fruit = data.nutrient(Nutrient.FRUIT) / n;
            this.vegetables = data.nutrient(Nutrient.VEGETABLES) / n;
            this.protein = data.nutrient(Nutrient.PROTEIN) / n;
            this.dairy = data.nutrient(Nutrient.DAIRY) / n;
            this.decay = data.decayModifier();
            return this;
        }

        public Builder multipliedFrom(Food food, int n) {
            return multipliedFrom(parent.readTfcFoodJson(food), n);
        }


        public Builder multipliedFrom(FoodData data, int n) {
            if (n <= 0) throw new IllegalArgumentException("n must be >= 1");
            this.hunger = data.hunger() * n;
            this.saturation = data.saturation() * n;
            this.water = data.water() * n;
            this.grain = data.nutrient(Nutrient.GRAIN) * n;
            this.fruit = data.nutrient(Nutrient.FRUIT) * n;
            this.vegetables = data.nutrient(Nutrient.VEGETABLES) * n;
            this.protein = data.nutrient(Nutrient.PROTEIN) * n;
            this.dairy = data.nutrient(Nutrient.DAIRY) * n;
            this.decay = data.decayModifier();
            return this;
        }

        public Builder nutrientMultiplier(float f){
            this.nutrient_multipler = f;
            return this;
        }

        public Builder type(String type){
            this.type = type;
            return this;
        }


        /** 用單一 Item 作為 ingredient */
        public Builder item(Item item) {
            this.ingredient = ingredientOf(item);
            this.item = item;
            return this;
        }

        /** 用 tag 作為 ingredient，例如 "forge:crops/tomato" */
        public Builder tag(String tagPath) {
            this.ingredient = ingredientTag(tagPath);
            return this;
        }

        /** 進階：直接塞自定義的 ingredient JSON（可為陣列做 OR 等） */
        public Builder ingredient(JsonElement ingredientJson) {
            this.ingredient = ingredientJson;
            return this;
        }

        public Builder setHunger(int hunger) {
            this.hunger = hunger;
            return this;
        }

        public Builder setSaturation(double saturation) {
            this.saturation = saturation;
            return this;
        }

        public Builder setWater(double water) {
            this.water = water;
            return this;
        }

        /** 預設 1，不需要就別呼叫 */
        public Builder setDecay(double decayModifier) {
            this.decay = decayModifier;
            return this;
        }

        public Builder setGrain(double grain) {
            this.grain = grain;
            return this;
        }

        public Builder setFruit(double fruit) {
            this.fruit = fruit;
            return this;
        }

        public Builder setVegetables(double vegetables) {
            this.vegetables = vegetables;
            return this;
        }

        public Builder setProtein(double protein) {
            this.protein = protein;
            return this;
        }

        public Builder setDairy(double dairy) {
            this.dairy = dairy;
            return this;
        }


        private static double round1(double value) {
            return Math.round(value * 10.0) / 10.0;
        }
        /** 產生 JSON 物件但不存入 Provider（若你想先檢視用） */
        public JsonObject buildJson() {
            if (ingredient == null) {
                throw new IllegalStateException("Food item json must contain 'ingredient'");
            }
            JsonObject root = new JsonObject();
            root.add("ingredient", ingredient);
            root.addProperty("hunger", hunger);
            root.addProperty("saturation", round1(saturation));
            root.addProperty("water", water);
            root.addProperty("decay_modifier", round1(decay));
            if(grain > 0) root.addProperty("grain", round1(grain * nutrient_multipler));
            if(fruit > 0) root.addProperty("fruit", round1(fruit * nutrient_multipler));
            if(vegetables > 0) root.addProperty("vegetables", round1(vegetables * nutrient_multipler));
            if(protein > 0) root.addProperty("protein", round1(protein * nutrient_multipler));
            if(dairy > 0) root.addProperty("dairy", round1(dairy * nutrient_multipler));
            if(type != null) root.addProperty("type", type);
            return root;
        }

        /** 寫回 Provider 的 entries，回傳 Provider 方便繼續串其他東西 */
        public SDFoodDataProvider save() {
            JsonObject root = buildJson();
            if(item != null) SDFoodAndRecipeGenerator.foodDataMap.put(item, readJson(root));
            parent.entries.put(parent.id(id), root);
            return parent;
        }

        /**
         * 將來源食物的五大營養值乘上 factor 後加入目前 builder，
         * 並將飽食與含水量直接相加，最後將 hunger 設為目前與來源中較大的那個。
         * decay 不變動。
         *
         * @param data   來源的 FoodData
         * @param factor 營養加成倍率（例如 0.5f = 取一半營養）
         * @return this
         */
        public Builder addNutrientsAndSetMaxHunger(FoodData data, float factor) {
            if ((double) factor < 0) throw new IllegalArgumentException("factor must be >= 0");

            // 1) 五大營養：乘以 factor 後相加

            this.grain += data.nutrient(Nutrient.GRAIN) * factor;
            this.fruit += data.nutrient(Nutrient.FRUIT) * factor;
            this.vegetables += data.nutrient(Nutrient.VEGETABLES) * factor;
            this.protein += data.nutrient(Nutrient.PROTEIN) * factor;
            this.dairy += data.nutrient(Nutrient.DAIRY) * factor;

            // 2) 飽食與含水：直接相加（不乘以 factor）
            this.saturation  += data.saturation();
            this.water       += data.water();

            // 3) 飢餓值：取最大
            this.hunger       = Math.max(this.hunger, data.hunger());

            return this;
        }

        /**
         * 同上，但輸入為 TFC 的 Food enum。
         */
        public Builder addNutrientsAndSetMaxHunger(Food food, float factor) {
            return addNutrientsAndSetMaxHunger(parent.readTfcFoodJson(food), factor);
        }

    }

}

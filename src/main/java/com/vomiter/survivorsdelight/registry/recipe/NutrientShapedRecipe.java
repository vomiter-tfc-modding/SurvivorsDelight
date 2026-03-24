package com.vomiter.survivorsdelight.registry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vomiter.survivorsdelight.registry.SDRecipeSerializers;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodComponent;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.component.item.ItemListComponent;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class NutrientShapedRecipe implements CraftingRecipe {
    private final ShapedRecipe vanilla;
    private final float balanceFactor;
    private final float presetDecay;
    private final int presetHunger;

    public NutrientShapedRecipe(ShapedRecipe vanilla, float balanceFactor, int presetHunger, float presetDecay) {
        this.vanilla = vanilla;
        this.balanceFactor = balanceFactor;
        this.presetHunger = presetHunger;
        this.presetDecay = presetDecay;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level) {
        return vanilla.matches(input, level);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, @NotNull HolderLookup.Provider provider) {
        ItemStack out = vanilla.assemble(input, provider).copy();
        if (out.isEmpty()) {
            return ItemStack.EMPTY;
        }

        List<FoodData> data = new ArrayList<>();
        for (ItemStack s : input.items()) {
            if (s.isEmpty()) continue;

            final FoodComponent foodComponent = s.get(TFCComponents.FOOD);
            if (foodComponent != null) {
                data.add(foodComponent.getData());
            }
        }

        if (data.isEmpty()) {
            return out;
        }

        final List<ItemStack> ingredients = input.items().stream()
            .filter(s -> !s.isEmpty())
            .collect(Collectors.groupingBy(ItemStack::getItem, Collectors.summingInt(ItemStack::getCount)))
            .entrySet().stream()
            .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparing(ItemStack::getCount)
                .thenComparing(item -> Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.getItem()))))
            .toList();

        final int foodCount = data.size();
        float factor = 1f - (this.balanceFactor * foodCount);
        if (factor < 0f) factor = 0f;

        int hunger = 0;
        float saturation = 0f;
        float water = 0;
        float[] nutrients = new float[Nutrient.VALUES.length];

        for (var d : data) {
            if (this.presetHunger == -1) hunger = Math.max(d.hunger(), hunger);
            saturation += d.saturation();
            water += d.water();
            for (int i = 0; i < nutrients.length; i++) {
                nutrients[i] += d.nutrient(Nutrient.VALUES[i]) * factor / (float) out.getCount();
            }
        }

        if (this.presetHunger != -1) hunger = presetHunger;
        hunger = Math.round(hunger / (float) out.getCount());
        saturation = Math.round(saturation / out.getCount());
        water = Math.round(water / out.getCount());

        FoodData merged = new FoodData(hunger, water, saturation, 0, nutrients, presetDecay);

        out.set(TFCComponents.INGREDIENTS, ItemListComponent.of(ingredients));
        FoodCapability.setFoodForDynamicItemOnCreate(out, merged);

        return out;
    }

    @Override public boolean canCraftInDimensions(int w, int h) { return vanilla.canCraftInDimensions(w, h); }
    @Override public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider) { return vanilla.getResultItem(provider); }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return SDRecipeSerializers.NUTRITION_CRAFTING.get(); }
    @Override public @NotNull RecipeType<?> getType() { return RecipeType.CRAFTING; }
    @Override public @NotNull CraftingBookCategory category() { return vanilla.category(); }
    @Override public @NotNull String getGroup() { return vanilla.getGroup(); }
    @Override public boolean isIncomplete() { return vanilla.isIncomplete(); }
    @Override public @NotNull NonNullList<Ingredient> getIngredients() { return vanilla.getIngredients(); }

    // ============ Serializer ============
    public static class Serializer implements RecipeSerializer<NutrientShapedRecipe> {
        private static final MapCodec<NutrientShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ShapedRecipe.CODEC.fieldOf("vanilla").forGetter(r -> r.vanilla),
            Codec.FLOAT.fieldOf("balance_factor").orElse(0.04f).forGetter(r -> r.balanceFactor),
            Codec.INT.fieldOf("hunger").orElse(-1).forGetter(r -> r.presetHunger),
            Codec.FLOAT.fieldOf("decay").orElse(4.5f).forGetter(r -> r.presetDecay)
        ).apply(instance, (vanilla, bf, hunger, decay) -> new NutrientShapedRecipe((ShapedRecipe) vanilla, bf, hunger, decay)));

        @Override
        public @NotNull MapCodec<NutrientShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, NutrientShapedRecipe> streamCodec() {
            return StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);
        }

        private static NutrientShapedRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buf) {
            ShapedRecipe vanilla = (ShapedRecipe) ShapedRecipe.STREAM_CODEC.decode(buf);
            float bf = buf.readFloat();
            int presetHunger = buf.readInt();
            float presetDecay = buf.readFloat();
            return new NutrientShapedRecipe(vanilla, bf, presetHunger, presetDecay);
        }

        private static void toNetwork(@NotNull RegistryFriendlyByteBuf buf, @NotNull NutrientShapedRecipe recipe) {
            ShapedRecipe.STREAM_CODEC.encode(buf, recipe.vanilla);
            buf.writeFloat(recipe.balanceFactor);
            buf.writeInt(recipe.presetHunger);
            buf.writeFloat(recipe.presetDecay);
        }
    }

    public static final class Builder {
        private final ItemStack result;
        private final int resultCount;
        private final List<String> pattern = new ArrayList<>();
        private final Map<Character, Ingredient> keys = new LinkedHashMap<>();

        private @Nullable String group;
        private float balance = 0.04f;
        private int presetHunger = -1;
        private float presetDecay = 4.5f;
        private CraftingBookCategory category = CraftingBookCategory.MISC;

        public Builder(ItemStack result, int resultCount) {
            this.result = result;
            this.resultCount = resultCount;
        }

        public static Builder shaped(ItemStack result, int resultCount) {
            return new Builder(result, resultCount);
        }

        public Builder group(String g) { this.group = g; return this; }
        public Builder row(String r) { this.pattern.add(r); return this; }
        public Builder define(char c, Ingredient i) { this.keys.put(c, i); return this; }
        public Builder category(CraftingBookCategory cat) { this.category = cat; return this; }
        public Builder balance(float f) { this.balance = f; return this; }
        public Builder hunger(int v) { this.presetHunger = v; return this; }
        public Builder decay(float v) { this.presetDecay = v; return this; }

        public void save(RecipeOutput out, ResourceLocation id) {
            // 1) pattern + keys -> ShapedRecipePattern
            final ShapedRecipePattern pat = ShapedRecipePattern.of(this.keys, this.pattern);
            // 2) 組 vanilla ShapedRecipe（用 1.21 的建構子）
            final ItemStack res = this.result.copy();
            res.setCount(this.resultCount);
            final ShapedRecipe vanilla = new ShapedRecipe(this.group == null ? "" : this.group, this.category, pat, res);
            // 3) 包成你的 NutrientShapedRecipe 後丟給 out
            final NutrientShapedRecipe wrapped = new NutrientShapedRecipe(vanilla, this.balance, this.presetHunger, this.presetDecay);
            out.accept(id, wrapped, null);
        }
    }

}

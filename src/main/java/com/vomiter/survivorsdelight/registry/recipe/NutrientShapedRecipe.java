package com.vomiter.survivorsdelight.registry.recipe;

import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.registry.SDRecipeSerializers;
import net.dries007.tfc.common.capabilities.food.*;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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

    @Override public boolean matches(@NotNull CraftingContainer inv, @NotNull Level level) {
        boolean anyRot = inv.getItems().stream().anyMatch(item -> {
            IFood food = FoodCapability.get(item);
            if(food == null) return false;
            return food.isRotten();
        });
        if(anyRot) return false;
        return vanilla.matches(inv, level);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingContainer inv, @NotNull RegistryAccess ra) {
        ItemStack out = vanilla.assemble(inv, ra).copy();

        List<FoodData> data = new ArrayList<>();
        List<ItemStack> ingredients = new ArrayList<>();

        final int slots = inv.getContainerSize();
        for (int i = 0; i < slots; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;

            var fh = FoodCapability.get(s);
            if (fh == null) continue;
            data.add(fh.getData());

            boolean merged = false;
            for (int j = 0; j < ingredients.size(); j++) {
                ItemStack e = ingredients.get(j);
                if (ItemStack.isSameItem(e, s)) {
                    ItemStack copy = e.copyWithCount(1);
                    copy.setCount(e.getCount() + 1); // 每格代表消耗 1
                    ingredients.set(j, copy);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                ItemStack one = s.copyWithCount(1);
                ingredients.add(one);
            }


        }
        ingredients.sort(Comparator.comparing(ItemStack::getCount)
                .thenComparing(item -> Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.getItem()))));


        final int foodCount = data.size();
        float factor = 1f - (this.balanceFactor * foodCount);
        if (factor < 0f) factor = 0f;

        int hunger = 0;
        float saturation = 0f;
        float water = 0;
        float[] nutrients = new float[Nutrient.VALUES.length];

        for (var d : data) {
            if(this.presetHunger == -1) hunger = Math.max(d.hunger(), hunger);
            saturation += d.saturation();
            water += d.water();
            for (int i = 0; i < nutrients.length; i++) {
                nutrients[i] += d.nutrient(net.dries007.tfc.common.capabilities.food.Nutrient.VALUES[i]) * factor;
            }
        }

        if(this.presetHunger != -1) hunger = presetHunger;
        hunger = Math.round(hunger / (float)out.getCount());
        saturation = Math.round(saturation / out.getCount());
        water = Math.round(water / out.getCount());

        var outFood = FoodCapability.get(out);
        if (outFood instanceof FoodHandler.Dynamic outDynamic) {
            FoodData merged =
                    new FoodData(
                            hunger, saturation, water ,nutrients[0], nutrients[1], nutrients[2], nutrients[3], nutrients[4], presetDecay
                    );
            outDynamic.setIngredients(ingredients);
            outDynamic.setFood(merged);
        }

        return out;
    }

    @Override public boolean canCraftInDimensions(int w, int h) { return vanilla.canCraftInDimensions(w, h); }
    @Override public @NotNull ItemStack getResultItem(@NotNull RegistryAccess ra) { return vanilla.getResultItem(ra); }
    @Override public @NotNull ResourceLocation getId() { return vanilla.getId(); }
    @Override public @NotNull RecipeSerializer<?> getSerializer() { return SDRecipeSerializers.NUTRITION_CRAFTING.get(); }
    @Override public @NotNull RecipeType<?> getType() { return RecipeType.CRAFTING; }

    @Override
    public @NotNull CraftingBookCategory category() {
        return CraftingBookCategory.MISC;
    }

    @Override public @NotNull String getGroup() { return vanilla.getGroup(); }
    @Override public boolean isIncomplete() { return vanilla.isIncomplete(); }
    @Override public @NotNull NonNullList<Ingredient> getIngredients() { return vanilla.getIngredients(); }

    // ============ Serializer ============
    public static class Serializer implements RecipeSerializer<NutrientShapedRecipe> {
        @Override
        public @NotNull NutrientShapedRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            // 先讓 vanilla 解析 key/pattern/result
            ShapedRecipe vanilla = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            float bf = GsonHelper.getAsFloat(json, "balance_factor", 0.04f);
            int presetHunger = GsonHelper.getAsInt(json, "hunger", -1);
            float presetDecay = GsonHelper.getAsFloat(json, "decay", 4.5f);
            return new NutrientShapedRecipe(vanilla, bf, presetHunger, presetDecay);
        }

        @Override
        public NutrientShapedRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            ShapedRecipe vanilla = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            float bf = buf.readFloat();
            int presetHunger = buf.readInt();
            float presetDecay = buf.readFloat();
            return new NutrientShapedRecipe(vanilla, bf, presetHunger, presetDecay);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, NutrientShapedRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe.vanilla);
            buf.writeFloat(recipe.balanceFactor);
            buf.writeInt(recipe.presetHunger);
            buf.writeFloat(recipe.presetDecay);
        }
    }
}

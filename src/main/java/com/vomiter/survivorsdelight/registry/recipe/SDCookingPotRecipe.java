package com.vomiter.survivorsdelight.registry.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vomiter.survivorsdelight.common.device.cooking_pot.wrap.ICookingPotRecipeFluidAccess;
import com.vomiter.survivorsdelight.registry.SDRecipeSerializers;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.FoodComponent;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.Nutrient;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

// 若你有自訂的 wrapper（例如可讀取鍋內流體的 wrapper），請替換這個 import
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.Optional;

public class SDCookingPotRecipe extends CookingPotRecipe {
    private final String group;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;
    private final ItemStack containerOverride; // 保留 JSON 指定容器
    private final int cookingTime;
    private final float experience;
    @Nullable private final FluidIngredient fluid;
    private final int fluidAmountMb;

    public SDCookingPotRecipe(
            String group,
            NonNullList<Ingredient> ingredients,
            ItemStack result,
            @Nullable ItemStack container, // 允許 JSON 省略
            int cookingTime,
            float experience,
            @Nullable FluidIngredient fluid,
            int fluidAmountMb
    ) {
        // 注意參數順序：... , container, experience, cookTime
        super(group, /*tab*/ null, ingredients,
                result,
                container == null ? ItemStack.EMPTY : container,
                experience,
                cookingTime);

        this.group = group;
        this.ingredients = NonNullList.of(Ingredient.EMPTY, ingredients.toArray(Ingredient[]::new));
        this.result = result.copy();
        this.containerOverride = container == null ? ItemStack.EMPTY : container.copy();
        this.cookingTime = cookingTime;
        this.experience = experience;
        this.fluid = fluid;
        this.fluidAmountMb = fluidAmountMb;
    }

    // ---- 需要流體時在這裡加判斷 ----
    @Override
    public boolean matches(@NotNull RecipeWrapper inv, @NotNull Level level) {
        if (!super.matches(inv, level)) return false;

        if (fluid == null || fluidAmountMb <= 0) return true;

        if (inv instanceof ICookingPotRecipeFluidAccess acc) {
            return acc.matchesFluid(fluid, fluidAmountMb);
        }
        return false; // 沒有流體能力就不匹配
    }

    // ---- 讓 FD 沿用原本類型 ----
    @Override public @NotNull RecipeType<?> getType() { return ModRecipeTypes.COOKING.get(); }
    @Override public @NotNull String getGroup() { return group; }
    @Override public @NotNull NonNullList<Ingredient> getIngredients() { return ingredients; }
    @Override public @NotNull ItemStack assemble(@NotNull RecipeWrapper input, HolderLookup.@NotNull Provider provider) { return result.copy(); }
    @Override public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) { return result.copy(); }
    @Override public @NotNull RecipeSerializer<?> getSerializer() {return SDRecipeSerializers.SD_COOKING_POT.get();}
    // 方便 builder / 其他邏輯取用
    @Nullable public FluidIngredient getFluid() { return fluid; }
    public int getFluidAmountMb() { return fluidAmountMb; }
    public ItemStack getResultStack() { return result.copy(); }
    public ItemStack getContainerOverrideStack() { return containerOverride.copy(); }
    public int getCookingTime() { return cookingTime; }
    public float getExperience() { return experience; }
    public boolean shouldCalcDynamic(){
        FoodComponent foodComponent = result.get(TFCComponents.FOOD);
        if(foodComponent == null) return true;
        FoodData foodData = foodComponent.getData();
        float anyValue = 0;
        anyValue += foodData.hunger();
        anyValue += foodData.saturation();
        anyValue += foodData.water();
        anyValue += foodData.intoxication();
        for (Nutrient value : Nutrient.VALUES) {
            anyValue += foodData.nutrient(value);
        }
        return anyValue == 0;
    }

    // ---- 1.21：MapCodec + StreamCodec ----
    public enum Serializer implements RecipeSerializer<SDCookingPotRecipe> {
        INSTANCE;

        public static final MapCodec<SDCookingPotRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(SDCookingPotRecipe::getGroup),
                        // 與 FD 保持一致：LIST_CODEC_NONEMPTY -> NonNullList
                        Ingredient.LIST_CODEC_NONEMPTY.fieldOf("ingredients")
                                .xmap(list -> {
                                    NonNullList<Ingredient> nnl = NonNullList.create();
                                    nnl.addAll(list);
                                    return nnl;
                                }, nnl -> nnl)
                                .forGetter(SDCookingPotRecipe::getIngredients),
                        ItemStack.STRICT_CODEC.fieldOf("result").forGetter(SDCookingPotRecipe::getResultStack),
                        ItemStack.STRICT_CODEC.optionalFieldOf("container", ItemStack.EMPTY)
                                .forGetter(r -> r.containerOverride.isEmpty() ? ItemStack.EMPTY : r.containerOverride.copy()),
                        Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(SDCookingPotRecipe::getCookingTime),
                        Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(SDCookingPotRecipe::getExperience),
                        FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(r -> Optional.ofNullable(r.getFluid())),
                        Codec.INT.optionalFieldOf("fluid_amount", 0).forGetter(SDCookingPotRecipe::getFluidAmountMb)
                ).apply(inst, (group, ings, res, containerItem, time, exp, optFluid, amt) ->
                        new SDCookingPotRecipe(
                                group, ings, res,
                                containerItem.isEmpty() ? null : containerItem,
                                time, exp,
                                optFluid.orElse(null), amt
                        )
                ));

        public static final StreamCodec<RegistryFriendlyByteBuf, SDCookingPotRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork0, Serializer::fromNetwork0);

        @Override public @NotNull MapCodec<SDCookingPotRecipe> codec() { return CODEC; }
        @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, SDCookingPotRecipe> streamCodec() { return STREAM_CODEC; }

        private static void toNetwork0(RegistryFriendlyByteBuf buf, SDCookingPotRecipe r) {
            buf.writeUtf(r.getGroup());

            buf.writeVarInt(r.getIngredients().size());
            for (Ingredient ing : r.getIngredients()) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ing);
            }

            ItemStack.STREAM_CODEC.encode(buf, r.getResultStack());

            // 與 FD 一致：OPTIONAL_STREAM_CODEC（用 EMPTY 當哨兵）
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, r.containerOverride);

            buf.writeFloat(r.getExperience());
            buf.writeVarInt(r.getCookingTime());

            buf.writeBoolean(r.getFluid() != null);
            if (r.getFluid() != null) {
                FluidIngredient.STREAM_CODEC.encode(buf, r.getFluid());
            }
            buf.writeVarInt(r.getFluidAmountMb());
        }

        private static SDCookingPotRecipe fromNetwork0(RegistryFriendlyByteBuf buf) {
            String group = buf.readUtf();

            int n = buf.readVarInt();
            NonNullList<Ingredient> ings = NonNullList.withSize(n, Ingredient.EMPTY);
            for (int i = 0; i < n; i++) {
                ings.set(i, Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
            }

            ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
            ItemStack container = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);

            float exp = buf.readFloat();
            int time = buf.readVarInt();

            FluidIngredient fluid = null;
            if (buf.readBoolean()) {
                fluid = FluidIngredient.STREAM_CODEC.decode(buf);
            }
            int amt = buf.readVarInt();

            return new SDCookingPotRecipe(
                    group, ings, result,
                    container.isEmpty() ? null : container,
                    time, exp,
                    fluid, amt
            );
        }
    }
}

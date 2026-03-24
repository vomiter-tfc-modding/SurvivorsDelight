package com.vomiter.survivorsdelight.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.IFood;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.common.recipes.ingredients.AndIngredient;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifierType;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifiers;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;


public class SDUtils {
    public static ItemStackModifierType<?> getModifierType(ResourceLocation id, RegistryAccess access) {
        Registry<ItemStackModifierType<?>> reg = access.registryOrThrow(ItemStackModifiers.KEY);
        return reg.get(id); // 可能回傳 null（id 不存在）
    }
    public static ItemStackModifier decodeModifier(ResourceLocation id, JsonElement data, RegistryAccess access) {
        ItemStackModifierType<?> type = getModifierType(id, access);
        if (type == null) return null; // or throw

        JsonElement payload = (data == null ? JsonNull.INSTANCE : data);
        @SuppressWarnings("unchecked")
        ItemStackModifier result = (ItemStackModifier) ((ItemStackModifierType) type)
                .codec().codec()
                .parse(JsonOps.INSTANCE, payload)
                .getOrThrow();
        return result;
    }

    public static class SDNotRottenIngredient{
        public static Ingredient of(Ingredient ingredient){
            return AndIngredient.of(ingredient, NotRottenIngredient.INSTANCE);
        }
        public static Ingredient of(Item item){
            return AndIngredient.of(Ingredient.of(item), NotRottenIngredient.INSTANCE);
        }
        public static Ingredient of(TagKey<Item> tagKey){
            return AndIngredient.of(Ingredient.of(tagKey), NotRottenIngredient.INSTANCE);
        }
    }

    public static Holder<Enchantment> getEnchantHolder(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(key);
    }

    public static JsonElement ingredientToJsonElement(Ingredient ing){
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ing).getOrThrow();
    }

    public static String ingredientToJsonString(Ingredient ing) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ing)
                .map(JsonElement::toString).getOrThrow();
    }



    public static Item getTFCFoodItem(Food food){
        return TFCItems.FOOD.get(food).get();
    }
    public static float getExtraNutrientAfterCooking(ItemStack rawStack, Nutrient nutrient, Level level) {
        if (level == null || rawStack.isEmpty()) {
            return 0f;
        }

        float rawValue = getNutrient(rawStack, nutrient);

        RecipeManager rm = level.getRecipeManager();
        List<RecipeHolder<HeatingRecipe>> allHeating = rm.getAllRecipesFor(TFCRecipeTypes.HEATING.get());

        HeatingRecipe matched = null;
        for (RecipeHolder<HeatingRecipe> recipeHolder : allHeating) {
            var recipe = recipeHolder.value();
            Ingredient ing = recipe.getIngredient();
            if (ing.test(rawStack)) {
                matched = recipe;
                break;
            }
        }

        if (matched == null) {
            return 0f;
        }

        ItemStack cookedStack = matched.assembleItem(rawStack);
        if (cookedStack.isEmpty()) {
            return 0f;
        }

        float cookedValue = getNutrient(cookedStack, nutrient);

        float diff = cookedValue - rawValue;
        return Math.max(diff, 0f);
    }

    // 小工具：從一個 stack 拿出指定營養素
    private static float getNutrient(ItemStack stack, Nutrient nutrient) {
        IFood food = FoodCapability.get(stack);
        if(food == null) return 0f;
        return food.getData().nutrient(nutrient);
    }

    public static class TagUtils{
        public static boolean fluidIngredientMatchesTag(RegistryAccess access,
                                                        SizedFluidIngredient ingredient,
                                                        TagKey<Fluid> fluidTag) {
            final var lookup = access.lookupOrThrow(Registries.FLUID);
            final var named  = lookup.get(fluidTag).orElse(null);
            if (named == null) return false;

            final int amount = Math.max(1, ingredient.amount());
            for (Holder<Fluid> holder : named) {
                if (ingredient.test(new FluidStack(holder.value(), amount))) {
                    return true;
                }
            }
            return false;
        }

        public static TagKey<Item> itemTag(String namespace, String path) {
            return TagKey.create(Registries.ITEM, RLUtils.build(namespace, path));
        }

        public static TagKey<Block> blockTag(String namespace, String path) {
            return TagKey.create(Registries.BLOCK, RLUtils.build(namespace, path));
        }

        public static TagKey<Fluid> fluidTag(String namespace, String path) {
            return TagKey.create(Registries.FLUID, RLUtils.build(namespace, path));
        }



    }

    @SuppressWarnings("all")
    public static class RLUtils {
        public static ResourceLocation build(String namespace, String path){
            return ResourceLocation.fromNamespaceAndPath(namespace, path);
        }

        public static ResourceLocation build(String path){
            return ResourceLocation.fromNamespaceAndPath(SurvivorsDelight.MODID, path);
        }
    }
}

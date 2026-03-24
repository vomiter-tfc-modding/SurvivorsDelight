package com.vomiter.survivorsdelight.data.recipe.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dries007.tfc.common.recipes.outputs.CopyFoodModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** 小型轉寫器：把 ISP 與其 modifiers 轉成與 TFC fromJson 相容的 JSON */
public final class SDJsonAdapters {
    private SDJsonAdapters() {}
    /*
    public static JsonObject stackToJson(ItemStack stack) {
        JsonObject obj = new JsonObject();
        obj.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (stack.getCount() != 1) {
            obj.addProperty("count", stack.getCount());
        }
        if (stack.hasTag() && !stack.getTag().isEmpty()) {
            obj.addProperty("tag", stack.getTag().toString());
        }
        return obj;
    }


    public static JsonElement writeISP(ItemStackProvider isp) {
        ItemStack stack = isp.stack().get();
        ItemStackModifier[] mods = isp.modifiers();

        if (mods == null || mods.length == 0) {
            return stackToJson(stack);
        }

        JsonObject obj = new JsonObject();
        obj.add("stack", stackToJson(stack));

        JsonArray arr = new JsonArray();
        for (ItemStackModifier m : mods) {
            arr.add(writeModifier(m));
        }
        obj.add("modifiers", arr);
        return obj;
    }

    private static JsonElement writeModifier(ItemStackModifier mod) {
        if (mod == CopyFoodModifier.INSTANCE) {
            return new com.google.gson.JsonPrimitive("tfc:copy_food");
        }
        throw new IllegalArgumentException("Unsupported ItemStackModifier: " + mod.getClass().getName());
    }

     */
}

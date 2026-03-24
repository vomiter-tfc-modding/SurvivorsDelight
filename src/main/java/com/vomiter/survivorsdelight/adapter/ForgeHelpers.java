package com.vomiter.survivorsdelight.adapter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ForgeHelpers {
    public static ResourceLocation getItemResourceLocation(Item item){
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static Item getItem(ResourceLocation rl){
        return BuiltInRegistries.ITEM.get(rl);
    }

}

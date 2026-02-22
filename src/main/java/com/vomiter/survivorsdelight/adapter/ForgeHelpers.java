package com.vomiter.survivorsdelight.adapter;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class ForgeHelpers {
    public static ResourceLocation getItemResourceLocation(Item item){
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static Item getItem(ResourceLocation rl){
        return ForgeRegistries.ITEMS.getValue(rl);
    }

}

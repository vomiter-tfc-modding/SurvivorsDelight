package com.vomiter.survivorsdelight.common.food;

import com.vomiter.survivorsdelight.registry.SDDataComponents;
import com.vomiter.survivorsdelight.registry.component.SDContainer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public interface IConsumableRemainder extends IItemStackExtension {
    @Override
    default @NotNull ItemStack getCraftingRemainingItem(){
        if((Object)this instanceof ItemStack itemStack){
            if(itemStack.getItem() instanceof ConsumableItem){
                SDContainer sdContainer = itemStack.get(SDDataComponents.FOOD_CONTAINER);
                if(sdContainer != null){
                    return new ItemStack(BuiltInRegistries.ITEM.get(sdContainer.itemId()));
                }
            }

        }
        return IItemStackExtension.super.getCraftingRemainingItem();
    }
}
package com.vomiter.survivorsdelight.mixin.food.remainder;

import com.vomiter.survivorsdelight.common.food.IConsumableRemainder;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public abstract class ItemStack_RemainderMixin implements IConsumableRemainder {
    /*
    The method to get crafting remainder is overridden in the implemented interface.
     */

}

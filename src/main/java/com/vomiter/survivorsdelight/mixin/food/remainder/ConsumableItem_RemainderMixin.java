package com.vomiter.survivorsdelight.mixin.food.remainder;

import com.vomiter.survivorsdelight.common.food.IConsumableRemainder;
import org.spongepowered.asm.mixin.Mixin;
import vectorwing.farmersdelight.common.item.ConsumableItem;

@Mixin(ConsumableItem.class)
public abstract class ConsumableItem_RemainderMixin implements IConsumableRemainder {
    /*
    The method to get crafting remainder is overridden in the implemented interface.
     */

}

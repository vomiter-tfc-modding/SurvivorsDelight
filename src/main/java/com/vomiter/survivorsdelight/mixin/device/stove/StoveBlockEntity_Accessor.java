package com.vomiter.survivorsdelight.mixin.device.stove;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

@Mixin(value = StoveBlockEntity.class, remap = false)
public interface StoveBlockEntity_Accessor {
    @Accessor("cookingTimes")
    int[] getCookingTimes();

    @Accessor("cookingTimesTotal")
    int[] getCookingTimesTotal();
}
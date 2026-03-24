package com.vomiter.survivorsdelight.mixin.food.effect;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import vectorwing.farmersdelight.common.item.DrinkableItem;
import vectorwing.farmersdelight.common.item.MelonJuiceItem;

@Mixin(value = MelonJuiceItem.class, remap = false)
public abstract class MelonJuiceItemMixin extends DrinkableItem {

    MelonJuiceItemMixin(Properties properties) {
        super(properties);
    }

    @ModifyConstant(method = "affectConsumer", constant = @Constant(floatValue = 2.0f))
    private float adjustHeal(float original){
        return 4.0f;
    }

}

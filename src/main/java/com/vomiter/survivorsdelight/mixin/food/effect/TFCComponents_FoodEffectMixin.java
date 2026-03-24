package com.vomiter.survivorsdelight.mixin.food.effect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.dries007.tfc.common.component.TFCComponents;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TFCComponents.class)
public class TFCComponents_FoodEffectMixin {
    @WrapOperation(method = "onModifyDefaultComponentsAfterResourceReload", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/component/DataComponentPatch$Builder;set(Lnet/minecraft/core/component/DataComponentType;Ljava/lang/Object;)Lnet/minecraft/core/component/DataComponentPatch$Builder;", ordinal = 0, remap = true), remap = false)
    private static DataComponentPatch.Builder restoreFoodEffect(DataComponentPatch.Builder instance, DataComponentType<FoodProperties> component, Object value, Operation<DataComponentPatch.Builder> original, @Local Item item){
        FoodProperties prevFood = item.components().get(DataComponents.FOOD);
        if(prevFood == null) return original.call(instance, component, value);
        FoodProperties.Builder foodBuilder = new FoodProperties.Builder();
        prevFood.effects().forEach(possibleEffect -> {
            foodBuilder.effect(possibleEffect::effect, possibleEffect.probability());
        });
        instance.set(component, foodBuilder.build());
        return instance;
    }
}

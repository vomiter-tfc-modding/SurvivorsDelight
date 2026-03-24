package com.vomiter.survivorsdelight.mixin.food.mushroom;

import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class MushroomItem_PreventRottenPlace {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void preventRottenPlace(BlockPlaceContext blockPlaceContext, CallbackInfoReturnable<InteractionResult> cir){
        if(FoodCapability.isRotten(blockPlaceContext.getItemInHand())) cir.setReturnValue(InteractionResult.FAIL);
    }
}

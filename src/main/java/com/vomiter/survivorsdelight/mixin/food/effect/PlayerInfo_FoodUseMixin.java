package com.vomiter.survivorsdelight.mixin.food.effect;

import com.vomiter.survivorsdelight.common.food.FoodUseContext;
import net.dries007.tfc.common.component.food.IFood;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.player.PlayerInfo;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerInfo.class, remap = false)
public abstract class PlayerInfo_FoodUseMixin {
    @Shadow @Final private Player player;

    @Inject(method = "eat(Lnet/dries007/tfc/common/component/food/IFood;)V", at = @At("HEAD"))
    private void sdtfc$markNutritionApplied(IFood food, CallbackInfo ci) {
        FoodUseContext.markNutritionApplied(player);
    }
    @Inject(method = "eat(Lnet/dries007/tfc/common/component/food/FoodData;)V", at = @At("HEAD"))
    private void sdtfc$markDirectNutritionApplied(FoodData food, CallbackInfo ci) {
        FoodUseContext.markNutritionApplied(player);
    }

}

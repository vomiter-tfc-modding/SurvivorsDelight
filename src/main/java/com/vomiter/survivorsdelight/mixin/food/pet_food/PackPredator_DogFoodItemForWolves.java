package com.vomiter.survivorsdelight.mixin.food.pet_food;

import com.llamalad7.mixinextras.sugar.Local;
import net.dries007.tfc.common.entities.TFCEntities;
import net.dries007.tfc.common.entities.ai.predator.PackPredator;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import vectorwing.farmersdelight.common.item.DogFoodItem;

@Mixin(PackPredator.class)
public class PackPredator_DogFoodItemForWolves {
    @ModifyConstant(method = "mobInteract", constant = @Constant(floatValue = 0.1f))
    private float giveDogFoodToWolves(
            float constant,
            @Local(argsOnly = true) Player player,
            @Local(argsOnly = true) InteractionHand hand
            ){
        PackPredator target = (PackPredator) (Object) this;
        ItemStack itemStack = player.getItemInHand(hand);
        if(
                itemStack.getItem() instanceof DogFoodItem
                && target.getType().equals(TFCEntities.WOLF.get())
        ){
            return constant * 2;
        }

        return constant;
    }

}

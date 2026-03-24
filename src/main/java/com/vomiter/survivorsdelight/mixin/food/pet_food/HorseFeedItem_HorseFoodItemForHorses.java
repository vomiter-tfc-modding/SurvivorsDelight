package com.vomiter.survivorsdelight.mixin.food.pet_food;

import net.dries007.tfc.common.entities.livestock.horse.HorseProperties;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.item.HorseFeedItem;

@Mixin(HorseFeedItem.HorseFeedEvent.class)
public class HorseFeedItem_HorseFoodItemForHorses {

    @Inject(method = "onHorseFeedApplied", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER), cancellable = true)
    private static void giveDogFoodToDog(PlayerInteractEvent.EntityInteract event, CallbackInfo ci){
        Entity target = event.getTarget();
        if(target instanceof HorseProperties horse){
            if(horse.isHungry()){
                horse.eatFood(event.getItemStack().copy(), event.getHand(), event.getEntity());
            }
            if(horse.getFamiliarity() < horse.getAdultFamiliarityCap()){
                ci.cancel();
            }
        }
    }

}

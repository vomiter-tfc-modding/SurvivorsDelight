package com.vomiter.survivorsdelight.mixin.food.pet_food;

import net.dries007.tfc.common.entities.livestock.pet.Dog;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.item.DogFoodItem;

@Mixin(DogFoodItem.DogFoodEvent.class)
public class DogFoodItem_DogFoodItemForDogs {
    @Inject(method = "onDogFoodApplied", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V", shift = At.Shift.AFTER), cancellable = true)
    private static void giveDogFoodToDog(PlayerInteractEvent.EntityInteract event, CallbackInfo ci){
        Entity target = event.getTarget();
        if(target instanceof Dog dog){
            if(dog.isHungry()){
                dog.eatFood(event.getItemStack().copy(), event.getHand(), event.getEntity());
            }
            if(dog.getFamiliarity() < dog.getAdultFamiliarityCap()){
                ci.cancel();
            }
        }

    }
}
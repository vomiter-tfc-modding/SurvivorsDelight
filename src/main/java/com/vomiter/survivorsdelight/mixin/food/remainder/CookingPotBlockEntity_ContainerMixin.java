package com.vomiter.survivorsdelight.mixin.food.remainder;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.registry.SDDataComponents;
import com.vomiter.survivorsdelight.registry.component.SDContainer;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.component.Bowl;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.IFood;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntity_ContainerMixin {
    @Shadow private ItemStack mealContainerStack;

    @Shadow @Final private ItemStackHandler inventory;

    @Shadow public abstract ItemStack getMeal();

    @ModifyExpressionValue(method = "isContainerValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), remap = true)
    private boolean expandValidContainer(boolean original, @Local(argsOnly = true) ItemStack container){
        if(original) return true;
        if(mealContainerStack.is(Items.BOWL) && container.is(TFCBlocks.CERAMIC_BOWL.get().asItem())) return true;
        else return mealContainerStack.is(Items.GLASS_BOTTLE) && container.is(TFCItems.SILICA_GLASS_BOTTLE.get());
    }

    @Inject(method = "useHeldItemOnMeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), remap = true, cancellable = true)
    private void applyContainer(ItemStack container, CallbackInfoReturnable<ItemStack> cir){
        ItemStack mealStack = getMeal();
        if(mealStack.is(SDTags.ItemTags.create("tfc", "soups"))) {
            var mealToGive = mealStack.split(1);
            mealToGive.set(TFCComponents.BOWL, Bowl.of(container));
            cir.setReturnValue(mealToGive);
        } else {
            var mealToGive = mealStack.split(1);
            mealToGive.set(SDDataComponents.FOOD_CONTAINER, new SDContainer(BuiltInRegistries.ITEM.getKey(container.getItem())));
            cir.setReturnValue(mealToGive);
        }
    }

    @Inject(method = "useHeldItemOnMeal", at = @At("HEAD"), cancellable = true)
    private void checkContainerItemRotten(ItemStack container, CallbackInfoReturnable<ItemStack> cir){
        IFood containerFood = FoodCapability.get(container);
        if(containerFood != null && containerFood.isRotten()){
            cir.setReturnValue(container);
        }
    }

    @Inject(method = "useStoredContainersOnMeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", remap = true), cancellable = true)
    private void applyContainerStored(CallbackInfo ci){
        ItemStack mealStack = inventory.getStackInSlot(6);
        ItemStack containerInputStack = inventory.getStackInSlot(7);
        ItemStack outputStack = inventory.getStackInSlot(8);

        IFood containerFood = FoodCapability.get(containerInputStack);
        if(containerFood != null && containerFood.isRotten()){
            ci.cancel();
        }

        if(ItemStack.isSameItem(mealStack.getCraftingRemainingItem(), containerInputStack)) return;
        int smallerStackCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
        int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.getCount());

        if (outputStack.isEmpty()) {
            ItemStack mealToPut = mealStack.split(mealCount);
            if(mealStack.is(SDTags.ItemTags.create("tfc", "soups"))) {
                mealToPut.set(TFCComponents.BOWL, Bowl.of(containerInputStack));
            } else{
                mealToPut.set(SDDataComponents.FOOD_CONTAINER, new SDContainer(BuiltInRegistries.ITEM.getKey(containerInputStack.getItem())));
            }
            containerInputStack.shrink(mealCount);
            inventory.setStackInSlot(8, mealToPut);
        } else if (outputStack.getItem() == mealStack.getItem()) {
            ItemStack simMeal = mealStack.copy();
            if(mealStack.is(SDTags.ItemTags.create("tfc", "soups"))) {
                simMeal.set(TFCComponents.BOWL, Bowl.of(containerInputStack));
            } else{
                simMeal.set(SDDataComponents.FOOD_CONTAINER, new SDContainer(BuiltInRegistries.ITEM.getKey(containerInputStack.getItem())));
            }
            if(FoodCapability.areStacksStackableExceptCreationDate(simMeal, outputStack)){
                mealStack.shrink(mealCount);
                containerInputStack.shrink(mealCount);
                FoodCapability.mergeItemStacks(outputStack, simMeal);
            }
        }
        ci.cancel();
    }

    @Shadow protected abstract void ejectIngredientRemainder(ItemStack remainderStack);

    @Inject(
            method = "processCooking",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V",
                    remap = true
            )
    )
    private void beforeIngredientShrink(
            RecipeHolder<CookingPotRecipe> recipe, CookingPotBlockEntity cookingPot, CallbackInfoReturnable<Boolean> cir, @Local(name = "slotStack") ItemStack slotStack
    ) {
        if(slotStack.hasCraftingRemainingItem()) return;
        var bowlComponent = slotStack.get(TFCComponents.BOWL);
        if(bowlComponent != null) ejectIngredientRemainder(bowlComponent.stack());
    }


}

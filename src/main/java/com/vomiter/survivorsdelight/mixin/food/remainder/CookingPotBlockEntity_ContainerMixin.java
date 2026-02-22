package com.vomiter.survivorsdelight.mixin.food.remainder;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.common.food.FoodContainerExpansion;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntity_ContainerMixin {
    private static final int MEAL_SLOT = CookingPotBlockEntity.MEAL_DISPLAY_SLOT;   // 6
    private static final int CONTAINER_SLOT = CookingPotBlockEntity.CONTAINER_SLOT; // 7
    private static final int OUTPUT_SLOT = CookingPotBlockEntity.OUTPUT_SLOT;       // 8

    // NBT keys (keep your existing schema for compatibility)
    private static final String NBT_SOUP_BOWL = "bowl";
    private static final String NBT_CONTAINER = "Container";

    // Cache the tag key (避免每次 create)
    private static final TagKey<Item> TAG_TFC_SOUPS = SDTags.ItemTags.create("tfc", "soups");
    
    @Shadow private ItemStack mealContainerStack;

    @Shadow @Final private ItemStackHandler inventory;

    @Shadow public abstract ItemStack getMeal();

    @ModifyExpressionValue(method = "isContainerValid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"), remap = true)
    private boolean expandValidContainer(boolean original, @Local(argsOnly = true) ItemStack container){
        if(original) return true;
        return FoodContainerExpansion.isExtraValid(mealContainerStack.getItem(), container);
    }

    @Inject(method = "useHeldItemOnMeal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"), remap = true, cancellable = true)
    private void applyContainer(ItemStack container, CallbackInfoReturnable<ItemStack> cir){
        ItemStack mealStack = getMeal();
        if(mealStack.is(TAG_TFC_SOUPS)) {
            var mealToGive = mealStack.split(1);
            mealToGive.getOrCreateTag().put(NBT_SOUP_BOWL, container.split(1).serializeNBT());
            cir.setReturnValue(mealToGive);
        } else if (!ItemStack.isSameItem(mealStack.getCraftingRemainingItem(), container)) {
            var mealToGive = mealStack.split(1);
            mealToGive.getOrCreateTag().put(NBT_CONTAINER, container.split(1).serializeNBT());
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
        ItemStack mealStack = inventory.getStackInSlot(MEAL_SLOT);
        ItemStack containerInputStack = inventory.getStackInSlot(CONTAINER_SLOT);
        ItemStack outputStack = inventory.getStackInSlot(OUTPUT_SLOT);

        IFood containerFood = FoodCapability.get(containerInputStack);
        if(containerFood != null && containerFood.isRotten()){
            ci.cancel();
        }

        if(ItemStack.isSameItem(mealStack.getCraftingRemainingItem(), containerInputStack)) return;
        int smallerStackCount = Math.min(mealStack.getCount(), containerInputStack.getCount());
        int mealCount = Math.min(smallerStackCount, mealStack.getMaxStackSize() - outputStack.getCount());

        if (outputStack.isEmpty()) {
            ItemStack mealToPut = mealStack.split(mealCount);
            if(mealStack.is(TAG_TFC_SOUPS)) {
                mealToPut.getOrCreateTag().put(NBT_SOUP_BOWL, containerInputStack.copyWithCount(1).serializeNBT());
            } else{
                mealToPut.getOrCreateTag().put(NBT_CONTAINER, containerInputStack.getItem().getDefaultInstance().serializeNBT());
            }
            containerInputStack.shrink(mealCount);
            inventory.setStackInSlot(OUTPUT_SLOT, mealToPut);
        } else if (outputStack.getItem() == mealStack.getItem()) {
            ItemStack simMeal = mealStack.copy();
            if(mealStack.is(TAG_TFC_SOUPS)) {
                simMeal.getOrCreateTag().put(NBT_SOUP_BOWL, containerInputStack.copyWithCount(1).serializeNBT());
            } else{
                simMeal.getOrCreateTag().put(NBT_CONTAINER, containerInputStack.copyWithCount(1).serializeNBT());
            }
            if(FoodCapability.areStacksStackableExceptCreationDate(simMeal, outputStack)){
                mealStack.shrink(mealCount);
                containerInputStack.shrink(mealCount);
                FoodCapability.mergeItemStacks(outputStack, simMeal);
            }
        }
        ci.cancel();
    }

}

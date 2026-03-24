package com.vomiter.survivorsdelight.mixin.food;

import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.IFood;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.registry.ModItems;

@Mixin(Item.class)
public class Item_ThrowableRottenTomatoMixin {
    @Unique
    private static final ThreadLocal<Boolean> SDTFC$IN_REDIRECT = ThreadLocal.withInitial(() -> false);

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void hijackRottenTomato(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir){
        if (SDTFC$IN_REDIRECT.get()) return; // 重入保護

        ItemStack item = player.getItemInHand(hand);
        if(!item.is(TFCItems.FOOD.get(Food.TOMATO).get())) return;
        IFood food = FoodCapability.get(item);
        if(food == null) return;
        if(!food.isRotten()) return;
        try {
            SDTFC$IN_REDIRECT.set(true);
            InteractionResultHolder<ItemStack> res = ModItems.ROTTEN_TOMATO.get().use(level, player, hand);
            cir.setReturnValue(res);
        } finally {
            SDTFC$IN_REDIRECT.set(false);
        }
    }
}

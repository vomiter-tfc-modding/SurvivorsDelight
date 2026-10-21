package com.vomiter.survivorsdelight.mixin.food.effect;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.vomiter.survivorsdelight.common.food.FoodUseContext;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.player.IPlayerInfo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntity_AddEatEffectMixin {
    @Shadow protected ItemStack useItem;

    @WrapMethod(method = "completeUsingItem")
    private void sdtfc$completeUsingItem(Operation<Void> original) {
        if (!((Object) this instanceof ServerPlayer player) || !FoodCapability.has(useItem)
                || !player.isUsingItem() || !useItem.equals(player.getItemInHand(player.getUsedItemHand()))) {
            original.call();
            return;
        }
        try (FoodUseContext context = FoodUseContext.begin(player, useItem)) {
            original.call();
            if (!context.nutritionApplied()) IPlayerInfo.get(player).eat(context.food());
        }
    }

    @WrapMethod(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")
    private boolean sdtfc$addEffect(MobEffectInstance effect, @Nullable Entity source, Operation<Boolean> original) {
        if (FoodUseContext.isRottenFood((LivingEntity) (Object) this) && effect.getEffect().value().isBeneficial()) return false;
        return original.call(effect, source);
    }

    @WrapMethod(method = "heal")
    private void sdtfc$heal(float amount, Operation<Void> original) {
        if (!FoodUseContext.isRottenFood((LivingEntity) (Object) this)) original.call(amount);
    }
}

package com.vomiter.survivorsdelight.mixin.device.skillet;

import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletUtil;
import com.vomiter.survivorsdelight.adapter.skillet.skillet_item.SkilletCookingCap;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.HeatHelper;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.utility.TextUtils;

@Mixin(value = SkilletItem.class)
public abstract class SkilletItem_TFCHeatMixin {
    @Unique private static final String KEY_COOKING = "Cooking";

    @Unique
    private float sdtfc$getTemperatureNearby(Player player, LevelReader level) {
        BlockPos pos = player.blockPosition();
        float temperature = 0;
        for (BlockPos posNearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            temperature = Math.max(temperature, HeatHelper.getTemperature(posNearby, level, HeatHelper.GetterType.IN_HAND));
        }
        return temperature;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void sdtfc$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack skilletStack = player.getItemInHand(hand);
        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack heatingStack = player.getItemInHand(otherHand);
        if(skilletStack.getItem() instanceof SDSkilletItem sdSkilletItem){
            if(!sdSkilletItem.canCook(skilletStack)){
                cir.setReturnValue(InteractionResultHolder.fail(skilletStack));
                return;
            }
        }

        if (heatingStack.isEmpty()) return;
        float temperatureNearby = sdtfc$getTemperatureNearby(player, level);
        if (temperatureNearby <= 0) {
            if(skilletStack.getEnchantmentLevel(Enchantments.FIRE_ASPECT) >= 1){
                temperatureNearby = 300;
            }
            else {
                return;
            }
        }

        if (player.isUnderWater()) {
            player.displayClientMessage(TextUtils.getTranslation("item.skilletStack.underwater"), true);
            return;
        }
        HeatingRecipe recipe = HeatingRecipe.getRecipe(new ItemStackInventory(heatingStack));
        if (recipe == null) return;
        if (!level.isClientSide) {
            ItemStack unit = heatingStack.split(1);
            if (unit.isEmpty()) return;
            IHeat heat = HeatCapability.get(unit);
            if (heat == null) {
                heatingStack.grow(1);
                return;
            }
            HeatCapability.addTemp(heat, temperatureNearby);
            CompoundTag tag = skilletStack.getOrCreateTag();
            tag.put(KEY_COOKING, unit.serializeNBT());
            var data = SkilletCookingCap.get(player);
            data.setCooking(unit);
            data.setTargetTemperature(recipe.getTemperature());
            data.setHand(hand);
        }
        player.startUsingItem(hand);
        cir.setReturnValue(InteractionResultHolder.pass(skilletStack));
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void sdtfc$onUseTick(Level level, LivingEntity living, ItemStack skilletStack, int remainingUseTicks, CallbackInfo ci) {
        if (!(living instanceof Player player)) return;
        var data = SkilletCookingCap.get(player);
        ItemStack cooking = data.getCooking();
        if (cooking.isEmpty()) return;
        float temperatureNearby = sdtfc$getTemperatureNearby(player, level);
        if (temperatureNearby <= 0) {
            if(skilletStack.getEnchantmentLevel(Enchantments.FIRE_ASPECT) >= 1){
                temperatureNearby = 300;
            }
            else {
                return;
            }
        }
        IHeat heat = HeatCapability.get(cooking);
        if (!level.isClientSide) {
            if (heat == null) {
                if (!player.addItem(cooking)) player.drop(cooking, false);
                data.clear();
                if (player.isUsingItem()) player.stopUsingItem();
                return;
            }

            HeatCapability.addTemp(heat, temperatureNearby);
            if(heat.getTemperature() < data.getTargetTemperature()) return;

            HeatingRecipe recipe = HeatingRecipe.getRecipe(new ItemStackInventory(cooking));
            if (recipe != null && recipe.isValidTemperature(heat.getTemperature())) {
                ItemStack result = recipe.assemble(new ItemStackInventory(cooking), level.registryAccess());
                if (!result.isEmpty()) {
                    FoodCapability.applyTrait(result, SkilletUtil.skilletCooked);
                    FoodCapability.updateFoodDecayOnCreate(result);
                    if (!player.addItem(result)) {
                        player.drop(result, false);
                    }
                }
                CompoundTag tag = skilletStack.getOrCreateTag();
                if(tag.contains(KEY_COOKING)){
                    tag.remove(KEY_COOKING);
                }

                if(skilletStack.getItem() instanceof SDSkilletItem){
                    skilletStack.hurtAndBreak(1, player, user -> {});

                    if(skilletStack.is(SDTags.ItemTags.RETURN_COPPER_SKILLET) && !(((SDSkilletItem) skilletStack.getItem()).canCook(skilletStack))){
                        InteractionHand hand = player.getUsedItemHand();
                        CompoundTag ctag = skilletStack.serializeNBT();
                        ctag.putString("id", SkilletMaterial.COPPER.location().toString());
                        ItemStack newSkilletStack = ItemStack.of(ctag);
                        newSkilletStack.setDamageValue(0);
                        player.broadcastBreakEvent(hand);
                        player.setItemInHand(hand, newSkilletStack);
                    }
                }
                data.clear();
                if (player.isUsingItem()) player.stopUsingItem();
            }
        }
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void sdtfc$releaseUsing(ItemStack skilletStack, Level level, LivingEntity living, int timeLeft, CallbackInfo ci) {
        if (!(living instanceof Player player)) return;
        var data = SkilletCookingCap.get(player);
        CompoundTag tag = skilletStack.getOrCreateTag();
        if(tag.contains(KEY_COOKING)){
            ItemStack fakeCookingStack = ItemStack.of(tag.getCompound(KEY_COOKING));
            HeatingRecipe recipe = HeatingRecipe.getRecipe(new ItemStackInventory(fakeCookingStack));
            if (recipe == null) return;
            if(!level.isClientSide){
                ItemStack cooking = data.getCooking();
                if (!player.addItem(cooking)) player.drop(cooking, false);
            }
            data.clear();
            tag.remove(KEY_COOKING);
            ci.cancel();
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void sdtfc$getUseDuration(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(20 * 60 * 60 * 3);
    }
}

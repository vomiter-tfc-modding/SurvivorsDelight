package com.vomiter.survivorsdelight.mixin.device.skillet;

import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletUtil;
import com.vomiter.survivorsdelight.common.device.skillet.itemcooking.ISkilletItemCookingData;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.SkilletProgressionBarS2CPayload;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.HeatHelper;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
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
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;
import vectorwing.farmersdelight.common.utility.TextUtils;

@Mixin(value = SkilletItem.class)
public abstract class SkilletItem_TFCHeatMixin {

    @Unique
    private static final ResourceLocation USE_SKILLET_ADV_ID =
            ResourceLocation.fromNamespaceAndPath("farmersdelight", "main/use_skillet");

    @Unique
    private static void sdtfc$awardAdvancement(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder adv = player.server.getAdvancements().get(id);
        if (adv == null) return; // 找不到就不做（避免 NPE）

        var progress = player.getAdvancements().getOrStartProgress(adv);
        if (progress.isDone()) return;

        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(adv, criterion);
        }
    }

    @Unique
    private float sdtfc$getTemperatureNearby(Player player, LevelReader level) {
        BlockPos pos = player.blockPosition();
        float temperature = 0;
        for (BlockPos posNearby : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            temperature = Math.max(temperature, HeatHelper.getTemperature(posNearby, level, HeatHelper.GetterType.IN_HAND));
        }
        return temperature;
    }

    @Unique
    private void sdtfc$abortAndReturn(Player player, ItemStack skilletStack, ISkilletItemCookingData data) {
        ItemStack cooking = data.getCooking();
        // Detach the real ingredient before inventory insertion or release callbacks.
        data.clear();
        if (player instanceof ServerPlayer serverPlayer) {
            if (!cooking.isEmpty() && !player.addItem(cooking)) player.drop(cooking, false);
            SDNetwork.sendToClient(serverPlayer, new SkilletProgressionBarS2CPayload(player.getUsedItemHand(), 0));
        }
        if (player.isUsingItem() && player.getUseItem() == skilletStack) player.stopUsingItem();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void sdtfc$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack skilletStack = player.getItemInHand(hand);
        if (!((Object) skilletStack instanceof ISkilletItemCookingData data)) return;
        if (skilletStack.getItem() instanceof SDSkilletItem sdSkilletItem && !sdSkilletItem.canCook(skilletStack)) {
            cir.setReturnValue(InteractionResultHolder.fail(skilletStack));
            return;
        }
        if (data.isCooking()) {
            if (!level.isClientSide || skilletStack.has(ModDataComponents.SKILLET_INGREDIENT)) {
                player.startUsingItem(hand);
                cir.setReturnValue(InteractionResultHolder.pass(skilletStack));
                return;
            }
            // The server already completed this client's previous prediction.
            data.clear();
        }

        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack heatingStack = player.getItemInHand(otherHand);
        if (heatingStack.isEmpty()) return;
        float temperatureNearby = sdtfc$getTemperatureNearby(player, level);
        if (temperatureNearby <= 0) {
            if (skilletStack.getEnchantmentLevel(SDUtils.getEnchantHolder(level, Enchantments.FIRE_ASPECT)) < 1) return;
            temperatureNearby = 300;
        }
        if (player.isUnderWater()) {
            player.displayClientMessage(TextUtils.getTranslation("item.skilletStack.underwater"), true);
            return;
        }
        HeatingRecipe recipe = HeatingRecipe.getRecipe(heatingStack);
        if (recipe == null) return;
        ItemStack unit = heatingStack.split(1);
        if (unit.isEmpty()) return;
        IHeat heat = HeatCapability.get(unit);
        if (heat == null) {
            heatingStack.grow(1);
            return;
        }

        HeatCapability.addTemp(heat, temperatureNearby);
        data.setCooking(unit);
        data.setTargetTemperature(recipe.getTemperature());
        data.setCookingBarWidth(0);
        // This component is a display copy, never a source for returning food.
        skilletStack.set(ModDataComponents.SKILLET_INGREDIENT, new ItemStackWrapper(unit.copy()));
        if (player instanceof ServerPlayer serverPlayer) {
            SDNetwork.sendToClient(serverPlayer, new SkilletProgressionBarS2CPayload(hand, 0));
        }
        player.startUsingItem(hand);
        cir.setReturnValue(InteractionResultHolder.pass(skilletStack));
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void sdtfc$onUseTick(Level level, LivingEntity living, ItemStack skilletStack, int remainingUseTicks, CallbackInfo ci) {
        if (!(living instanceof Player player)) return;
        if (!((Object) skilletStack instanceof ISkilletItemCookingData data)) return;
        ItemStack cooking = data.getCooking();
        if (cooking.isEmpty()) return;
        if (level.isClientSide && !skilletStack.has(ModDataComponents.SKILLET_INGREDIENT)) {
            data.clear();
            return;
        }
        float temperatureNearby = sdtfc$getTemperatureNearby(player, level);
        if (temperatureNearby <= 0) {
            if (skilletStack.getEnchantmentLevel(SDUtils.getEnchantHolder(level, Enchantments.FIRE_ASPECT)) < 1) return;
            temperatureNearby = 300;
        }
        IHeat heat = HeatCapability.get(cooking);
        if (heat == null) {
            if (!level.isClientSide) sdtfc$abortAndReturn(player, skilletStack, data);
            return;
        }
        // Match 1.20.1's local prediction; only the server produces results.
        HeatCapability.addTemp(heat, temperatureNearby);
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        InteractionHand hand = player.getUsedItemHand();
        int width = Math.round(heat.getTemperature() / data.getTargetTemperature() * 13);
        SDNetwork.sendToClient(serverPlayer, new SkilletProgressionBarS2CPayload(hand, width));
        if (heat.getTemperature() < data.getTargetTemperature()) return;
        HeatingRecipe recipe = HeatingRecipe.getRecipe(cooking);
        if (recipe == null || !recipe.isValidTemperature(heat.getTemperature())) return;
        ItemStack result = recipe.assembleItem(cooking);
        // Clear ownership before callbacks, inventory insertion, or replacing the skillet.
        data.clear();
        SDNetwork.sendToClient(serverPlayer, new SkilletProgressionBarS2CPayload(hand, 0));
        if (!result.isEmpty()) {
            FoodCapability.applyTrait(result, SkilletUtil.skilletCooked);
            FoodCapability.setCreatedNow(result);
            if (!player.addItem(result)) player.drop(result, false);
        }

        EquipmentSlot equipmentSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        if (skilletStack.getItem() instanceof SDSkilletItem sdSkilletItem) {
            skilletStack.hurtAndBreak(1, player, equipmentSlot);
            if (skilletStack.is(SDTags.ItemTags.RETURN_COPPER_SKILLET) && !sdSkilletItem.canCook(skilletStack)) {
                CompoundTag tag = (CompoundTag) skilletStack.save(level.registryAccess());
                tag.putString("id", SkilletMaterial.COPPER.location().toString());
                ItemStack newSkilletStack = ItemStack.parseOptional(level.registryAccess(), tag);
                newSkilletStack.setDamageValue(0);
                player.onEquippedItemBroken(skilletStack.getItem(), equipmentSlot);
                player.setItemInHand(hand, newSkilletStack);
            }
        }
        sdtfc$awardAdvancement(serverPlayer, USE_SKILLET_ADV_ID);
        if (player.isUsingItem()) player.stopUsingItem();
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void sdtfc$releaseUsing(ItemStack skilletStack, Level level, LivingEntity living, int timeLeft, CallbackInfo ci) {
        if (!(living instanceof Player player)) return;
        if (!((Object) skilletStack instanceof ISkilletItemCookingData data)) return;
        ItemStack shown = skilletStack.getOrDefault(ModDataComponents.SKILLET_INGREDIENT, ItemStackWrapper.EMPTY).getStack();
        if (data.isCooking() || (!shown.isEmpty() && HeatingRecipe.getRecipe(shown) != null)) {
            // Cancel on both sides, so FD cannot separately return the display copy.
            sdtfc$abortAndReturn(player, skilletStack, data);
            ci.cancel();
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void sdtfc$getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(20 * 60 * 60 * 3);
    }
}

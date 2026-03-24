package com.vomiter.survivorsdelight.mixin.device.skillet;

import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletUtil;
import com.vomiter.survivorsdelight.common.device.skillet.itemcooking.SkilletCookingCap;
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

    /**
     * Server-only：安全中止流程（優先返還 CAP 真實材料；若 CAP 空但 display 有，fallback 還 display）
     * 並清乾淨：cap + display + cooking time。
     */
    @Unique
    private void sdtfc$abortAndReturn(ServerPlayer player, ItemStack skilletStack) {
        var data = SkilletCookingCap.get(player);

        // 真實材料（優先）
        ItemStack real = data.getCooking();

        // 顯示材料（fallback）
        ItemStackWrapper stored = skilletStack.getOrDefault(ModDataComponents.SKILLET_INGREDIENT, ItemStackWrapper.EMPTY);
        ItemStack shown = stored.getStack();

        ItemStack toReturn = !real.isEmpty() ? real : shown;
        if (!toReturn.isEmpty()) {
            ItemStack ret = toReturn.copy();
            if (!player.addItem(ret)) player.drop(ret, false);
        }

        // 清狀態
        data.clear();
        skilletStack.remove(ModDataComponents.SKILLET_INGREDIENT);
        skilletStack.remove(ModDataComponents.COOKING_TIME_LENGTH);

        if (player.isUsingItem()) player.stopUsingItem();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void sdtfc$use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack skilletStack = player.getItemInHand(hand);
        InteractionHand otherHand = (hand == InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack heatingStack = player.getItemInHand(otherHand);

        if (skilletStack.getItem() instanceof SDSkilletItem sdSkilletItem) {
            if (!sdSkilletItem.canCook(skilletStack)) {
                cir.setReturnValue(InteractionResultHolder.fail(skilletStack));
                return;
            }
        }

        if (heatingStack.isEmpty()) return;

        float temperatureNearby = sdtfc$getTemperatureNearby(player, level);
        if (temperatureNearby <= 0) {
            if (skilletStack.getEnchantmentLevel(SDUtils.getEnchantHolder(level, Enchantments.FIRE_ASPECT)) >= 1) {
                temperatureNearby = 300;
            } else {
                return;
            }
        }

        if (player.isUnderWater()) {
            player.displayClientMessage(TextUtils.getTranslation("item.skilletStack.underwater"), true);
            return;
        }

        HeatingRecipe recipe = HeatingRecipe.getRecipe(heatingStack);
        if (recipe == null) return;
        ItemStack unit = heatingStack.split(1);
        if (unit.isEmpty()) return;


        if (!level.isClientSide) {
            // 先扣 1 顆

            // 確認可加熱
            IHeat heat = HeatCapability.get(unit);
            if (heat == null) {
                heatingStack.grow(1);
                return;
            }

            // 初始化溫度（真實材料存在 CAP）
            HeatCapability.addTemp(heat, temperatureNearby);

            var data = SkilletCookingCap.get(player);
            data.setCooking(unit.copy()); // 你說 unit/copy 已驗證 OK，保留原寫法
            data.setTargetTemperature(recipe.getTemperature());
            data.setHand(hand);

            // display 只放 1 顆，讓 FD UI/邏輯正常（不會隨 tick 改變）
        }
        skilletStack.set(ModDataComponents.SKILLET_INGREDIENT, new ItemStackWrapper(unit.copy()));


        // 用原版流程維持使用中動畫/節奏
        player.startUsingItem(hand);
        cir.setReturnValue(InteractionResultHolder.consume(skilletStack));
        cir.cancel();
    }

    @Inject(method = "onUseTick", at = @At("HEAD"))
    private void sdtfc$onUseTick(Level level, LivingEntity living, ItemStack skilletStack, int remainingUseTicks, CallbackInfo ci) {
        if (!(living instanceof ServerPlayer player)) return;
        if (level.isClientSide) return;

        EquipmentSlot equipmentSlot = player.getUsedItemHand().equals(InteractionHand.MAIN_HAND) ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;

        var data = SkilletCookingCap.get(player);
        ItemStack cooking = data.getCooking();

        // 重要：CAP 空 -> 不做任何事（避免假顯示造成吞材料）
        if (cooking.isEmpty()) return;

        float temperatureNearby = sdtfc$getTemperatureNearby(player, level);
        if (temperatureNearby <= 0) {
            if (skilletStack.getEnchantmentLevel(SDUtils.getEnchantHolder(level, Enchantments.FIRE_ASPECT)) >= 1) {
                temperatureNearby = 300;
            } else {
                return;
            }
        }

        IHeat heat = HeatCapability.get(cooking);
        if (heat == null) {
            // CAP 材料狀態異常：直接中止並返還（這會同時清 display）
            sdtfc$abortAndReturn(player, skilletStack);
            return;
        }

        HeatCapability.addTemp(heat, temperatureNearby);
        if (heat.getTemperature() < data.getTargetTemperature()) return;

        HeatingRecipe recipe = HeatingRecipe.getRecipe(cooking);
        if (recipe != null && recipe.isValidTemperature(heat.getTemperature())) {
            ItemStack result = recipe.assembleItem(cooking);
            if (!result.isEmpty()) {
                FoodCapability.applyTrait(result, SkilletUtil.skilletCooked);
                if (!player.addItem(result)) {
                    player.drop(result, false);
                }
            }

            // 完成：清 display + cap
            skilletStack.remove(ModDataComponents.SKILLET_INGREDIENT);
            skilletStack.remove(ModDataComponents.COOKING_TIME_LENGTH);

            if (skilletStack.getItem() instanceof SDSkilletItem) {
                skilletStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

                if (skilletStack.is(SDTags.ItemTags.RETURN_COPPER_SKILLET)
                        && !(((SDSkilletItem) skilletStack.getItem()).canCook(skilletStack))) {

                    InteractionHand hand = player.getUsedItemHand();
                    var lookup = level.registryAccess();

                    CompoundTag tag = (CompoundTag) skilletStack.save(lookup);
                    tag.putString("id", SkilletMaterial.COPPER.location().toString());

                    ItemStack newSkilletStack = ItemStack.parseOptional(lookup, tag);
                    newSkilletStack.setDamageValue(0);

                    player.onEquippedItemBroken(skilletStack.getItem(), equipmentSlot);
                    player.setItemInHand(hand, newSkilletStack);
                }
            }

            data.clear();
            if (player.isUsingItem()) {
                sdtfc$awardAdvancement(player, USE_SKILLET_ADV_ID);
                player.stopUsingItem();
            };
        }
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"), cancellable = true)
    private void sdtfc$releaseUsing(ItemStack skilletStack, Level level, LivingEntity living, int timeLeft, CallbackInfo ci) {
        // 只允許 server 處理返還/清理，避免 client-side remove 造成不同步與「吞掉」體感
        if (!(living instanceof ServerPlayer player)) return;
        if (level.isClientSide) return;

        ItemStackWrapper storedStack = skilletStack.getOrDefault(ModDataComponents.SKILLET_INGREDIENT, ItemStackWrapper.EMPTY);

        // 若 display 有東西，代表在 FD 的「使用中」狀態：放手就要退料
        // 退料以 CAP 真實材料為優先（保底），並清掉 display/time
        if (!storedStack.getStack().isEmpty()) {
            sdtfc$abortAndReturn(player, skilletStack);
            ci.cancel();
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void sdtfc$getUseDuration(ItemStack stack, LivingEntity entity, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(20 * 60 * 60 * 3);
    }
}

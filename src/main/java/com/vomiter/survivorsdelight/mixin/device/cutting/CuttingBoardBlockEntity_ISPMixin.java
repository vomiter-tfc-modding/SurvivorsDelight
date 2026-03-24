package com.vomiter.survivorsdelight.mixin.device.cutting;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.cutting_board.CuttingBoardBlockEntityAdapter;
import com.vomiter.survivorsdelight.registry.recipe.SDCuttingRecipe;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModAdvancements;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(CuttingBoardBlockEntity.class)
public abstract class CuttingBoardBlockEntity_ISPMixin {

    @Shadow public abstract boolean isEmpty();

    @Shadow protected abstract Optional<RecipeHolder<CuttingBoardRecipe>> getMatchingRecipe(ItemStack toolStack, @org.jetbrains.annotations.Nullable Player player);

    @Inject(method = "processStoredItemUsingTool", at = @At("HEAD"), cancellable = true, remap = false)
    private void sd$processWithISP(ItemStack toolStack, @Nullable Player player, CallbackInfoReturnable<Boolean> cir) {
        CuttingBoardBlockEntity self = (CuttingBoardBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }

        // ★ client 端不跑 ISP，交給原版 + server 同步
        if (level.isClientSide()) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var optRecipe = getMatchingRecipe(toolStack, player);
        if (optRecipe.isEmpty()) {
            return; // 讓原版流程處理其他配方
        }
        if (!(optRecipe.get().value() instanceof SDCuttingRecipe recipe)) {
            return; // 非 SDCuttingRecipe -> 原版處理
        }

        CuttingBoardBlockEntityAdapter.cuttingBoardISP(toolStack, player, self, recipe);
        if (player instanceof ServerPlayer sp) {
            ModAdvancements.USE_CUTTING_BOARD.get().trigger(sp);
        }

        // 攔截原本的 processStoredItemUsingTool
        cir.setReturnValue(true);
    }
}

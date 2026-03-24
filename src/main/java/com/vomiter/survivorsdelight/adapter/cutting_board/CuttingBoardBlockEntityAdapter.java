package com.vomiter.survivorsdelight.adapter.cutting_board;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.registry.recipe.SDCuttingRecipe;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.registry.ModAdvancements;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CuttingBoardBlockEntityAdapter {
    public static void cuttingBoardISP(ItemStack toolStack, @Nullable Player player, CuttingBoardBlockEntity cuttingBoard, SDCuttingRecipe recipe){
        var level = cuttingBoard.getLevel();
        assert level != null;
        int fortune = EnchantmentHelper.getTagEnchantmentLevel(
                SDUtils.getEnchantHolder(level, Enchantments.FORTUNE),
                toolStack
        );
        double fortuneBonus = Configuration.CUTTING_BOARD_FORTUNE_BONUS.get() * (double) fortune;

        List<ItemStack> out = new ArrayList<>();

        for (SDCuttingRecipe.Output r : recipe.getOutputs()) {
            // 1) 先用 ISP 算出實際要掉的東西（套好 modifier）
            ItemStack stack = r.getISPResult(level).getStack(cuttingBoard.getStoredItem());
            if (stack.isEmpty()) {
                continue;
            }

            // 2) 決定這個 output 的 base chance
            float baseChance = 1.0f;
            if (r instanceof SDCuttingRecipe.StackOutput stackOutput) {
                baseChance = stackOutput.chance();
            }
            // ProviderOutput 就當成 1.0f

            float totalChance = (float) (baseChance + fortuneBonus);
            // 安全一點夾在 [0, 1] 之間
            if (totalChance <= 0f) {
                continue;
            }
            if (totalChance > 1f) {
                totalChance = 1f;
            }

            float roll = level.random.nextFloat();
            // 3) 如果沒過機率就跳過這個 output
            if (roll >= totalChance) {
                continue;
            }

            out.add(stack);
        }

        // 這個 log 可以暫時留著看看
        SurvivorsDelight.LOGGER.info("[Cutting ISP] side=server, pos={}, outputs(before filter)={}, kept={}",
                cuttingBoard.getBlockPos(), recipe.getOutputs().size(), out.size());

        Direction dir = cuttingBoard.getBlockState().getValue(CuttingBoardBlock.FACING).getCounterClockWise();
        for (ItemStack resultStack : out) {
            ItemUtils.spawnItemEntity(
                    level,
                    resultStack.copy(),
                    cuttingBoard.getBlockPos().getX() + 0.5D + dir.getStepX() * 0.2D,
                    cuttingBoard.getBlockPos().getY() + 0.2D,
                    cuttingBoard.getBlockPos().getZ() + 0.5D + dir.getStepZ() * 0.2D,
                    dir.getStepX() * 0.2F, 0.0F, dir.getStepZ() * 0.2F
            );
        }

        toolStack.hurtAndBreak(1, (ServerLevel) level, player, (item) -> {});

        cuttingBoard.playProcessingSound(recipe.getSoundEvent().orElse(null), toolStack, cuttingBoard.getStoredItem());
        cuttingBoard.removeItem();

        if (player instanceof ServerPlayer sp) {
            ModAdvancements.USE_CUTTING_BOARD.get().trigger(sp);
        }

    }
}

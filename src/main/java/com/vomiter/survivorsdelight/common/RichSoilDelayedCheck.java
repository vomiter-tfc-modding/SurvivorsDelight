package com.vomiter.survivorsdelight.common;

import com.vomiter.survivorsdelight.SDConfig;
import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blocks.crop.CropBlock;
import net.dries007.tfc.common.blocks.crop.DeadCropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RichSoilDelayedCheck {

    private static final List<DelayedTask> TASKS = new ArrayList<>();

    public static boolean shouldDestroy(Level level, BlockPos pos){
        BlockEntity be = level.getBlockEntity(pos);
        boolean hasNoCropBE = !(be instanceof CropBlockEntity);
        Block taskBlock = level.getBlockState(pos).getBlock();
        boolean isNotDeadCrop = !(taskBlock instanceof DeadCropBlock);
        boolean isNotTFCCrop = !(taskBlock instanceof CropBlock);
        boolean isVanillaCrop = taskBlock instanceof BonemealableBlock;
        return hasNoCropBE && isNotDeadCrop && isNotTFCCrop && isVanillaCrop;
    }

    public static void onPlayerRightClick_RichSoilFarmGating(PlayerInteractEvent.RightClickBlock event) {
        if (SDConfig.RICH_SOIL_FARMLAND_ALLOW_NON_TFC_CROP) return;

        Level level = event.getLevel();
        BlockPos farmlandPos = event.getPos();

        if (!level.getBlockState(farmlandPos).is(ModBlocks.RICH_SOIL_FARMLAND.get())) return;
        if(!Direction.UP.equals(event.getHitVec().getDirection())) return;
        var usedItem = event.getItemStack().getItem();
        if(usedItem instanceof BlockItem blockItem){
            if(blockItem.getBlock() instanceof BonemealableBlock){
                if(!(blockItem.getBlock() instanceof CropBlock)) event.setCanceled(true);
            }
        } else {
            // 延遲 5 tick 檢查
            TASKS.add(new DelayedTask(level, farmlandPos.above(), 5));
        }
    }

    public static void onServerTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.level.isClientSide()) return;

        Iterator<DelayedTask> it = TASKS.iterator();
        while (it.hasNext()) {
            DelayedTask task = it.next();
            if (task.level != event.level) continue;

            task.delay--;
            if (task.delay > 0) continue;


            if (shouldDestroy(task.level, task.pos)) {
                task.level.destroyBlock(task.pos, true);
            }

            it.remove();
        }
    }

    private static class DelayedTask {
        final Level level;
        final BlockPos pos;
        int delay;

        DelayedTask(Level level, BlockPos pos, int delay) {
            this.level = level;
            this.pos = pos.immutable();
            this.delay = delay;
        }
    }
}
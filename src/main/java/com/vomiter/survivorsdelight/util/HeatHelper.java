package com.vomiter.survivorsdelight.util;

import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.tag.ModTags;

public class HeatHelper {
    public enum GetterType {
        BLOCK,
        IN_HAND,
        BOTH,
        BOTH_NOT
    }

    public static float getTemperature(BlockPos pos, LevelReader level, GetterType getterType){
        BlockState blockState = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        boolean isBlock = (getterType == GetterType.BLOCK || getterType == GetterType.BOTH);
        boolean isInHand = (getterType == GetterType.IN_HAND || getterType == GetterType.BOTH);

        if(isBlock && blockState.is(SDTags.BlockTags.HEAT_TO_BLOCK_BLACKLIST)){
            return 0;
        }

        if(isInHand && blockState.is(SDTags.BlockTags.HEAT_TO_IN_HAND_BLACKLIST)){
            return 0;
        }

        if(blockEntity instanceof HeatSourceBlockEntity heatSourceBlockEntity){
            return heatSourceBlockEntity.sdtfc$getTemperature();
        }
        if(blockState.is(SDTags.BlockTags.STATIC_HEAT_MODERATE)){
            return 500f;
        } else if (blockState.is(SDTags.BlockTags.STATIC_HEAT_HIGH)) {
            return 1500f;
        } else if (blockState.is(SDTags.BlockTags.STATIC_HEAT_LOW)){
            return 250f;
        }
        return 0;
    }

    public static float getTargetTemperature(BlockPos pos, LevelReader level, boolean requiresDirectHeat, GetterType getterType){
        if (level == null) return 0f;
        BlockPos below = pos.below();
        float heatBelow = HeatHelper.getTemperature(below, level, getterType);
        if(heatBelow > 0) return heatBelow;
        if(!requiresDirectHeat && level.getBlockState(below).is(ModTags.HEAT_CONDUCTORS)){
            return HeatHelper.getTemperature(below.below(), level, getterType);
        }
        return 0;
    }
}

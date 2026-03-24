package com.vomiter.survivorsdelight.compat.firmalife;

import com.eerussianguy.firmalife.common.blockentities.ApplianceBlockEntity;
import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.common.device.stove.IStoveBlockEntity;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

public class StoveOvenCompat {
    public static void ovenHeating(Level level, BlockPos pos, BlockState state, StoveBlockEntity stove){
        var self = (IStoveBlockEntity)stove;
        assert self != null;
        var above = level.getBlockEntity(pos.above());
        if(above instanceof ApplianceBlockEntity ovenTop) {
            float temperature = 0;
            temperature = ((HeatSourceBlockEntity) stove).sdtfc$getTemperature();
            HeatCapability.provideHeatTo(level, pos.above(), Direction.DOWN, ((HeatSourceBlockEntity) stove).sdtfc$getTemperature());
        }
    }

    public static void interactionRegister(){

    }
}

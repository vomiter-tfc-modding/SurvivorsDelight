package com.vomiter.survivorsdelight.compat.firmalife;

import com.eerussianguy.firmalife.common.blockentities.ApplianceBlockEntity;
import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.adapter.stove.IStoveBlockEntity;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

public final class StoveOvenCompat {
    private StoveOvenCompat() {
    }

    public static void ovenHeating(Level level, BlockPos pos, BlockState state, StoveBlockEntity stove) {
        if (!(stove instanceof IStoveBlockEntity) || !(stove instanceof HeatSourceBlockEntity heatSource)) {
            return;
        }

        final BlockEntity above = level.getBlockEntity(pos.above());
        if (!(above instanceof ApplianceBlockEntity<?> appliance)) {
            return;
        }

        boolean hasAnyItem = false;
        for (int i = 0; i < appliance.getInventory().getSlots(); i++) {
            if (!appliance.getInventory().getStackInSlot(i).isEmpty()) {
                hasAnyItem = true;
                break;
            }
        }
        if (!hasAnyItem) {
            return;
        }

        final float temperature = heatSource.sdtfc$getTemperature();
        if (temperature <= 0f) {
            return;
        }

        HeatCapability.provideHeatTo(level, pos.above(), Direction.DOWN, temperature);
    }

    public static void interactionRegister() {
    }
}

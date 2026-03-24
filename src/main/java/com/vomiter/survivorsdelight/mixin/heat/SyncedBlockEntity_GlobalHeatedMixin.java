package com.vomiter.survivorsdelight.mixin.heat;

import com.vomiter.survivorsdelight.util.HeatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

@Mixin(value = SyncedBlockEntity.class, remap = false)
public abstract class SyncedBlockEntity_GlobalHeatedMixin implements HeatableBlockEntity {
    @Override
    public boolean isHeated(Level level, BlockPos pos) {
        return HeatHelper.getTargetTemperature(pos, level, requiresDirectHeat(), HeatHelper.GetterType.BLOCK) >= 100;
    }
}

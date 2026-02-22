package com.vomiter.survivorsdelight.mixin.farming.farmland;

import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.adapter.farming.ClimateRangeBuilder;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mixin(value = FarmlandBlock.class, remap = false)
public class FarmlandBlock_RichSoilTooltip {
    @ModifyVariable(
            method = "getHydrationTooltip(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;Z)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"),
            argsOnly = true)
    private static ClimateRange expandHydration(ClimateRange value, @Local(argsOnly = true) LevelAccessor level, @Local(argsOnly = true)BlockPos pos){
        if(level.getBlockState(pos).is(ModBlocks.RICH_SOIL_FARMLAND.get())){
            return ClimateRangeBuilder.deriveLoose(value.getId());
        }
        return value;
    }

    @ModifyVariable(
            method = "getTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;Z)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"),
            argsOnly = true)
    private static ClimateRange expandTemperature(ClimateRange value, @Local(argsOnly = true) Level level, @Local(argsOnly = true)BlockPos pos){
        if(level.getBlockState(pos.below()).is(ModBlocks.RICH_SOIL_FARMLAND.get())){
            return ClimateRangeBuilder.deriveLoose(value.getId());
        }
        return value;
    }

}

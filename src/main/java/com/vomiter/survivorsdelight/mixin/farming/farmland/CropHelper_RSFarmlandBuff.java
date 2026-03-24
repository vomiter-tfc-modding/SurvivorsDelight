package com.vomiter.survivorsdelight.mixin.farming.farmland;

import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.common.farming.ClimateRangeBuilder;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.blocks.crop.CropHelpers;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vectorwing.farmersdelight.common.registry.ModBlocks;

@Mixin(value = CropHelpers.class, remap = false)
public class CropHelper_RSFarmlandBuff {
    @ModifyVariable(method = "growthTickStep", ordinal = 0, at = @At("STORE"))
    private static ClimateRange expandClimateRange(ClimateRange original, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos){
        if(level.getBlockState(pos.below()).is(SDTags.BlockTags.FARMERS_FARMLAND)){
            return ClimateRangeBuilder.deriveLoose(original);
        }
        return original;
    }
}

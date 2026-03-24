package com.vomiter.survivorsdelight.mixin.farming.farmland;

import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.common.farming.ClimateRangeBuilder;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.util.climate.ClimateRange;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vectorwing.farmersdelight.common.registry.ModBlocks;

// NOTE:
// TFC renamed getTemperatureTooltip -> getInstantTemperatureTooltip / getAverageTemperatureTooltip
// Keep multiple method targets + require = 0 for cross-version compatibility.
// pos may refer to crop position in some call paths, hence pos.below() is intentional.

@Mixin(value = FarmlandBlock.class, remap = false)
public class FarmlandBlock_RichSoilTooltip {

    @ModifyVariable(
            method = "getHydrationTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;Z)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private static ClimateRange expandHydration(
            ClimateRange value,
            @Local(argsOnly = true) Level level,
            @Local(argsOnly = true) BlockPos pos
    ) {
        if (level.getBlockState(pos).is(SDTags.BlockTags.FARMERS_FARMLAND)) {
            return ClimateRangeBuilder.deriveLoose(value);
        }
        return value;
    }

    @ModifyVariable(
            method = {
                    // 舊版（反編譯版）
                    "getTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;Z)Lnet/minecraft/network/chat/Component;",
                    // 新版（原始碼版）
                    "getInstantTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;Z)Lnet/minecraft/network/chat/Component;",
                    // 新版可能也會用到的平均溫度 tooltip
                    "getAverageTemperatureTooltip(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/dries007/tfc/util/climate/ClimateRange;Z)Lnet/minecraft/network/chat/Component;"
            },
            at = @At("HEAD"),
            argsOnly = true,
            require = 0
    )
    private static ClimateRange expandTemperature(
            ClimateRange value,
            @Local(argsOnly = true) Level level,
            @Local(argsOnly = true) BlockPos pos
    ) {
        // 保留你原本的 below 判斷（不改）
        if (level.getBlockState(pos.below()).is(SDTags.BlockTags.FARMERS_FARMLAND)) {
            return ClimateRangeBuilder.deriveLoose(value);
        }
        return value;
    }
}

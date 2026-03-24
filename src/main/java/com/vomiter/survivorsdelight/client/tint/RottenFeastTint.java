package com.vomiter.survivorsdelight.client.tint;

import com.vomiter.survivorsdelight.common.food.block.SDDecayingBlockEntity;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class RottenFeastTint {
    private RottenFeastTint() {}

    public static int getOverlayColorIfShouldTint(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SDDecayingBlockEntity decaying)) return 0;
        if (!decaying.isRotten()) return 0;

        return TFCConfig.CLIENT.foodExpiryOverlayColor.get(); // 0xAARRGGBB
    }
}

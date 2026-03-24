package com.vomiter.survivorsdelight.common.food.block;

import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DecayingPieBlockEntity extends SDDecayingBlockEntity {
    public DecayingPieBlockEntity(BlockPos pos, BlockState state) {
        super(SDBlockEntityTypes.PIE_DECAYING.get(), pos, state);
    }
}
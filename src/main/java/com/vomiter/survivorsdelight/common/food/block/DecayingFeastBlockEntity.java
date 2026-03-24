package com.vomiter.survivorsdelight.common.food.block;

import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class DecayingFeastBlockEntity extends SDDecayingBlockEntity {
    public DecayingFeastBlockEntity(BlockPos pos, BlockState state) {
        super(SDBlockEntityTypes.FEAST_DECAYING.get(), pos, state);
    }
}
package com.vomiter.survivorsdelight.common.device.skillet;

import com.vomiter.survivorsdelight.SDConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.lang.reflect.Method;

public final class SkilletSlotSizeLimitHandler extends ItemStackHandler {
    private final BlockEntity owner;

    public SkilletSlotSizeLimitHandler(BlockEntity owner) {
        super(1); // Skillet has only 1 slot
        this.owner = owner;
    }

    @Override
    protected void onContentsChanged(int slot) {
        try {
            Method m = owner.getClass().getDeclaredMethod("inventoryChanged");
            m.setAccessible(true);
            m.invoke(owner);
        } catch (Throwable ignored) {
            //if failed, send block update
            owner.setChanged();
            Level lvl = owner.getLevel();
            if (lvl != null) {
                BlockPos pos = owner.getBlockPos();
                BlockState state = owner.getBlockState();
                lvl.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }


    @Override
    public int getSlotLimit(int slot) {
        return SDConfig.SKILLET_SLOT_LIMIT;
    }
}
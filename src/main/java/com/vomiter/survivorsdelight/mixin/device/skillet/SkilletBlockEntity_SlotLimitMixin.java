package com.vomiter.survivorsdelight.mixin.device.skillet;

import com.vomiter.survivorsdelight.common.device.skillet.SkilletSlotSizeLimitHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

@Mixin(value = SkilletBlockEntity.class, remap = false)
public abstract class SkilletBlockEntity_SlotLimitMixin extends SyncedBlockEntity {
    public SkilletBlockEntity_SlotLimitMixin(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Inject(method = "createHandler", at = @At("HEAD"), cancellable = true)
    private void sdtfc$replaceHandler(CallbackInfoReturnable<ItemStackHandler> cir) {
        final SkilletBlockEntity owner = (SkilletBlockEntity) (Object) this;
        cir.setReturnValue(new SkilletSlotSizeLimitHandler(owner));
    }
}
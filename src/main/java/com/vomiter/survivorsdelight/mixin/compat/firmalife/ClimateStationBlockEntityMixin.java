package com.vomiter.survivorsdelight.mixin.compat.firmalife;

import com.eerussianguy.firmalife.common.blockentities.ClimateStationBlockEntity;
import com.eerussianguy.firmalife.common.blockentities.ClimateType;
import com.vomiter.survivorsdelight.compat.firmalife.SDClimateReceiver;
import com.vomiter.survivorsdelight.compat.firmalife.SDClimateType;
import net.dries007.tfc.common.blockentities.TFCBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(value = ClimateStationBlockEntity.class, remap = false)
public abstract class ClimateStationBlockEntityMixin extends TFCBlockEntity {
    @Shadow
    private Set<BlockPos> positions;

    @Shadow
    private ClimateType type;

    protected ClimateStationBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "updateValidity", at =@At("TAIL"))
    private void updateSDClimateReceiver(boolean valid, int tier, CallbackInfo ci){
        assert level != null;
        SDClimateType sdClimateType = SDClimateType.valueOf(type.name());
        positions.forEach(pos -> {
            final SDClimateReceiver receiver = SDClimateReceiver.get(level, pos);
            if (receiver != null)
            {
                receiver.setValid(level, pos, valid, tier, sdClimateType);
            }
        });
    }
}

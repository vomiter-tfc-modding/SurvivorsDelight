package com.vomiter.survivorsdelight.mixin.farming.farmland;

import com.vomiter.survivorsdelight.SDConfig;
import com.vomiter.survivorsdelight.common.RichSoilDelayedCheck;
import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blockentities.TickCounterBlockEntity;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;

import java.util.List;

@Mixin(RichSoilFarmlandBlock.class)
public class RichSoilFarmland_AddBlockEntityMixin extends FarmBlock implements EntityBlock, HoeOverlayBlock {
    public RichSoilFarmland_AddBlockEntityMixin(Properties p_53247_) {
        super(p_53247_);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FarmlandBlockEntity(pos, state);
    }

    @Inject(
            method = "randomTick",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void avoidCropBoneMealing(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci){
        var aboveBlockEntity = level.getBlockEntity(pos.above());
        if(aboveBlockEntity instanceof CropBlockEntity)ci.cancel();
        else if(aboveBlockEntity instanceof TickCounterBlockEntity) ci.cancel();
        else if(!SDConfig.RICH_SOIL_FARMLAND_ALLOW_NON_TFC_CROP){
            if (RichSoilDelayedCheck.shouldDestroy(level, pos.above())) {
                level.destroyBlock(pos.above(), true);
            }
        }
    }


    @Override
    public void addHoeOverlayInfo(Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull List<Component> text, boolean b) {
        level.getBlockEntity(blockPos, TFCBlockEntities.FARMLAND.get()).ifPresent(farmland -> farmland.addHoeOverlayInfo(level, blockPos, text, true, true));
    }
}

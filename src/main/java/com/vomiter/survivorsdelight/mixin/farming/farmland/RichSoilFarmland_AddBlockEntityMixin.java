package com.vomiter.survivorsdelight.mixin.farming.farmland;

import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.crop.CropHelpers;
import net.dries007.tfc.common.blocks.soil.HoeOverlayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.block.RichSoilFarmlandBlock;

import java.util.function.Consumer;

@Mixin(RichSoilFarmlandBlock.class)
public class RichSoilFarmland_AddBlockEntityMixin extends FarmBlock implements EntityBlock, HoeOverlayBlock {
    public RichSoilFarmland_AddBlockEntityMixin(Properties properties) {
        super(properties);
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
        if(level.getBlockEntity(pos.above()) instanceof CropBlockEntity){
            ci.cancel();
        }
    }


    @Override
    public void addHoeOverlayInfo(Level level, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Consumer<Component> consumer, boolean b) {
        level.getBlockEntity(blockPos, TFCBlockEntities.FARMLAND.get()).ifPresent(farmland -> farmland.addHoeOverlayInfo(level, blockPos, consumer, true, true));
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult)
    {
        return CropHelpers.useFertilizer(level, player, hand, pos)
                ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

}
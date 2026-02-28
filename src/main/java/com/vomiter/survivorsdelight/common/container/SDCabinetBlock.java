package com.vomiter.survivorsdelight.common.container;

import com.vomiter.survivorsdelight.adapter.container.CabinetAdapters;
import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.CabinetBlock;

import javax.annotation.Nullable;
import java.util.List;

public class SDCabinetBlock extends CabinetBlock {
    public SDCabinetBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return ((BlockEntityType<?>) SDBlockEntityTypes.SD_CABINET.get()).create(pos, state);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SDCabinetBlockEntity cabinet)) return InteractionResult.PASS;
        if(CabinetAdapters.checkCanTreat(level, pos, player, hand)) {
            cabinet.setTreated(true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            if (player instanceof ServerPlayer sp) {

                    NetworkHooks.openScreen(sp, cabinet, buf -> buf.writeBlockPos(pos));

            }
        }

        return InteractionResult.SUCCESS;
    }

    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if (tileEntity instanceof SDCabinetBlockEntity container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    container.removeStored(container.getItem(i));
                }
                Containers.dropContents(level, pos, container);
                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof SDCabinetBlockEntity cabinet && !drops.isEmpty()) {
            for (ItemStack drop : drops) {
                if (drop.getItem() == this.asItem() && cabinet.isTreated()) {
                    drop.getOrCreateTagElement("BlockEntityTag")
                            .putBoolean(SDCabinetBlockEntity.TAG_TREATED, cabinet.isTreated());
                    if (cabinet.hasCustomName()) drop.setHoverName(cabinet.getCustomName());
                }
            }
        }
        return drops;
    }

    @Override
    public void tick(@NotNull BlockState state, ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof SDCabinetBlockEntity sdCabinetBlockEntity) {
            sdCabinetBlockEntity.recheckOpen();
        }
    }

}

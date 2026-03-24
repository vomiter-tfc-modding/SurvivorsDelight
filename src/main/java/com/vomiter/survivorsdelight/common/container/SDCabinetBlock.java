package com.vomiter.survivorsdelight.common.container;

import com.vomiter.survivorsdelight.adapter.container.CabinetAdapters;
import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
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
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, Level level,
                                                       @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand,
                                                       @NotNull BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SDCabinetBlockEntity cabinet)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 已處理就讓其它互動流程繼續
        if (cabinet.isTreated()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 嘗試以手上物品進行「木材防腐處理」
        if (CabinetAdapters.tryTreatWithItem(player, hand, stack)) {
            cabinet.setTreated(true);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 不屬於處理劑的物品 -> 交回「預設方塊互動」(例如開櫃、擺放物品等)
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /* ========= 空手互動：開啟 GUI ========= */
    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                                     @NotNull Player player, @NotNull BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SDCabinetBlockEntity cabinet)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            player.openMenu(cabinet, buf -> buf.writeBlockPos(pos));
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
        BlockEntity be = builder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof SDCabinetBlockEntity cabinet && !drops.isEmpty()) {
            for (ItemStack drop : drops) {
                if (drop.getItem() == this.asItem()) {
                    CompoundTag tag = new CompoundTag();
                    tag.putBoolean(SDCabinetBlockEntity.TAG_TREATED, cabinet.isTreated());

                    BlockItem.setBlockEntityData(drop, cabinet.getType(), tag);

                    if (cabinet.hasCustomName()) {
                        drop.set(DataComponents.CUSTOM_NAME, cabinet.getCustomName());
                    }
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

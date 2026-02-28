package com.vomiter.survivorsdelight.common.container;

import com.vomiter.survivorsdelight.adapter.container.CabinetAdapters;
import com.vomiter.survivorsdelight.adapter.stack.FoodStackAdapters;
import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import com.vomiter.survivorsdelight.registry.SDContainerTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.stream.IntStream;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.Packet;


public class SDCabinetBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int ROWS = 2;
    public static final int COLS = 9;
    public static final String TAG_TREATED = "Treated";
    private static final int[] ALL_SLOTS = IntStream.range(0, ROWS * COLS).toArray();
    public void setStored(ItemStack food){
        if(this.TREATED) CabinetAdapters.setStored(food);
    }
    public void removeStored(ItemStack food){
        CabinetAdapters.removeStored(food);
    }

    void updateBlockOpenState(BlockState state, boolean open) {
        if (level != null) {
            this.level.setBlock(this.getBlockPos(), state.setValue(SDCabinetBlock.OPEN, open), 3);
        }
    }
    private void playSound(BlockState state, SoundEvent sound) {
        if (level == null) return;

        Vec3i cabinetFacingVector = state.getValue(SDCabinetBlock.FACING).getNormal();
        double x = (double) worldPosition.getX() + 0.5D + (double) cabinetFacingVector.getX() / 2.0D;
        double y = (double) worldPosition.getY() + 0.5D + (double) cabinetFacingVector.getY() / 2.0D;
        double z = (double) worldPosition.getZ() + 0.5D + (double) cabinetFacingVector.getZ() / 2.0D;
        level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, level.random.nextFloat() * 0.1F + 0.9F);
    }
    public void startOpen(@NotNull Player pPlayer) {
        if (level != null && !this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, level, this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(@NotNull Player pPlayer) {
        if (level != null && !this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, level, this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (level != null && !this.remove) {
            this.openersCounter.recheckOpeners(level, this.getBlockPos(), this.getBlockState());
        }
    }


    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter()
    {
        protected void onOpen(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            SDCabinetBlockEntity.this.playSound(state, ModSounds.BLOCK_CABINET_OPEN.get());
            SDCabinetBlockEntity.this.updateBlockOpenState(state, true);
        }

        protected void onClose(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state) {
            SDCabinetBlockEntity.this.playSound(state, ModSounds.BLOCK_CABINET_CLOSE.get());
            SDCabinetBlockEntity.this.updateBlockOpenState(state, false);
        }

        protected void openerCountChanged(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState sta, int arg1, int arg2) {
        }

        protected boolean isOwnContainer(Player p_155060_) {
            if (p_155060_.containerMenu instanceof SDCabinetMenu) {
                Container container = ((SDCabinetMenu) p_155060_.containerMenu).getContainer();
                return container == SDCabinetBlockEntity.this;
            } else {
                return false;
            }
        }
    };



    private boolean TREATED;
    public boolean isTreated() { return TREATED; }
    public void setTreated(boolean treated) {
        this.TREATED = treated;
        setChanged();
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(ROWS * COLS, ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items);
        }
        if (tag.contains(TAG_TREATED)) {
            this.TREATED = tag.getBoolean(TAG_TREATED);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items);
        }
        tag.putBoolean(TAG_TREATED, this.TREATED);
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return isValid(stack);
    }
    
    @Override
    public int @NotNull [] getSlotsForFace(@NotNull Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, @NotNull ItemStack stack, Direction side) {
        return isValid(stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int p_19239_, @NotNull ItemStack p_19240_, @NotNull Direction p_19241_) {
        return true;
    }


    public SDCabinetBlockEntity(BlockPos pos, BlockState state) {
        super(SDBlockEntityTypes.SD_CABINET.get(), pos, state);
    }

    private NonNullList<ItemStack> items = NonNullList.withSize(ROWS * COLS, ItemStack.EMPTY);

    @Override public int getContainerSize() { return items.size(); }
    @Override protected @NotNull NonNullList<ItemStack> getItems() { return items; }
    @Override protected void setItems(@NotNull NonNullList<ItemStack> items) { this.items = items; }

    @Override
    public void setItem(int slot, @NotNull ItemStack stack) {
        if (!stack.isEmpty() && !isValid(stack)) {
            return;
        }
        super.setItem(slot, stack);
        assert level != null;
        if (!level.isClientSide && !stack.isEmpty()) {
            setStored(stack);
        }
        setChanged();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        ItemStack removed = super.removeItem(slot, amount);
        assert level != null;
        if (!level.isClientSide && !removed.isEmpty()) {
            removeStored(removed);
        }
        setChanged();
        return removed;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = super.removeItemNoUpdate(slot);
        assert level != null;
        if (!level.isClientSide && !removed.isEmpty()) {
            removeStored(removed);
        }
        setChanged();
        return removed;
    }

    public static boolean isValid(ItemStack stack) {
        return CabinetAdapters.isValidItemInCabinet(stack);
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable("container.survivorsdelight.cabinet");
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int id, @NotNull Inventory inv) {
        return new SDCabinetMenu(SDContainerTypes.CABINET.get(), id, inv, this, 2);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable("container.survivorsdelight.cabinet");
    }

    /*
    =====
     */

    private final LazyOptional<IItemHandler> upHandler =
            LazyOptional.of(() -> new TraitItemHandler(this, Direction.UP));
    private final LazyOptional<IItemHandler> downHandler =
            LazyOptional.of(() -> new TraitItemHandler(this, Direction.DOWN));
    private final LazyOptional<IItemHandler> sideHandler =
            LazyOptional.of(() -> new TraitItemHandler(this, Direction.NORTH));

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.DOWN) return downHandler.cast();
            if (side == Direction.UP) return upHandler.cast();
            return sideHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        upHandler.invalidate();
        downHandler.invalidate();
        sideHandler.invalidate();
    }

    private static class TraitItemHandler extends SidedInvWrapper {
        private final SDCabinetBlockEntity be;

        public TraitItemHandler(SDCabinetBlockEntity be, Direction side) {
            super(be, side);
            this.be = be;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return isValid(stack);
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }

            ItemStack toInsert = stack.copy();

            if (be.level != null && !be.level.isClientSide) {
                be.setStored(toInsert);
            }

            // 1) merge into existing stacks
            for (int i = 0; i < getSlots(); i++) {
                if (toInsert.isEmpty()) break;

                ItemStack existing = getStackInSlot(i);
                if (!existing.isEmpty()
                        && FoodStackAdapters.stackableExceptCreationDate(existing, toInsert)) {

                    if (simulate) {
                        int moved = FoodStackAdapters.simulateMovedCount(existing, toInsert);
                        if (moved > 0) toInsert.shrink(moved);
                    } else {
                        FoodStackAdapters.mergeInto(existing, toInsert); // existing & toInsert will be mutated by TFC logic
                        setStackInSlot(i, existing);
                        if (be.level != null && !be.level.isClientSide) {
                            be.setStored(existing);
                        }
                        be.setChanged();
                    }
                }
            }

            // 2) fill empty slots
            for (int i = 0; i < getSlots(); i++) {
                if (toInsert.isEmpty()) break;

                ItemStack existing = getStackInSlot(i);
                if (existing.isEmpty() && isItemValid(i, toInsert)) {
                    if (simulate) {
                        int limit = Math.min(getSlotLimit(i), toInsert.getMaxStackSize());
                        int moved = Math.min(limit, toInsert.getCount());
                        if (moved > 0) toInsert.shrink(moved);
                    } else {
                        // 用同一套 merge 規則（creation_date 一致）
                        ItemStack newStack = FoodStackAdapters.mergeInto(ItemStack.EMPTY, toInsert);
                        setStackInSlot(i, newStack);
                        if (be.level != null && !be.level.isClientSide) {
                            be.setStored(newStack);
                        }
                        be.setChanged();
                    }
                }
            }

            if (be.level != null && !be.level.isClientSide) {
                be.removeStored(toInsert);
            }
            return toInsert;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack out = super.extractItem(slot, amount, simulate);
            if (!simulate && !out.isEmpty()) {
                assert be.level != null;
                if (!be.level.isClientSide) {
                    be.removeStored(out);
                    be.setChanged();
                }
            }
            return out;
        }

    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        // 這份 NBT 會被塞進 chunk packet 給 client
        return this.saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // 這是「單點更新」封包（setChanged + sendBlockUpdated 時會用到）
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag) {
        // client 收到 chunk packet 時走這裡
        this.load(tag);
    }

    @Override
    public void onDataPacket(net.minecraft.network.Connection net, ClientboundBlockEntityDataPacket pkt) {
        // client 收到單點更新封包時走這裡
        if(pkt.getTag() != null) this.load(pkt.getTag());
    }
}

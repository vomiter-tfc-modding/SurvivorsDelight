package com.vomiter.survivorsdelight.common.container;

import com.vomiter.survivorsdelight.adapter.container.CabinetAdapters;
import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import com.vomiter.survivorsdelight.registry.SDContainerTypes;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.EnumMap;
import java.util.stream.IntStream;

public class SDCabinetBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    public static final int ROWS = 2;
    public static final int COLS = 9;
    public static final String TAG_TREATED = "Treated";
    private static final int[] ALL_SLOTS = IntStream.range(0, ROWS * COLS).toArray();
    public void setStored(ItemStack food){
        if(this.isTreated()) CabinetAdapters.setStored(food);
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
    protected void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        this.items = NonNullList.withSize(ROWS * COLS, ItemStack.EMPTY);

        // 若仍沿用 loot table 流程，維持既有判斷
        if (!this.tryLoadLootTable(tag)) {
            // 1.21 版：用帶 registries 的多載
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }

        if (tag.contains(TAG_TREATED)) {
            this.TREATED = tag.getBoolean(TAG_TREATED);
        }
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        if (!this.trySaveLootTable(tag)) {
            // 1.21 版：用帶 registries 的多載（回傳值是同一個 tag，可忽略）
            ContainerHelper.saveAllItems(tag, this.items, registries);
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

    private final IItemHandler unsidedHandler = new TraitItemHandler(this, Direction.UP);
    private final EnumMap<Direction, IItemHandler> sidedHandlers = new EnumMap<>(Direction.class);

    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return unsidedHandler;
        }
        return sidedHandlers.computeIfAbsent(side, s -> new TraitItemHandler(this, s));
    }

    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                SDBlockEntityTypes.SD_CABINET.get(),
                SDCabinetBlockEntity::getItemHandler
        );
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
            // 不接受或空堆疊就直接返回
            if (stack.isEmpty() || !isItemValid(slot, stack)) {
                return stack;
            }

            // 注意：我們忽略傳進來的 slot，會在「可存取的所有槽位」上跑 merge→填空
            ItemStack toInsert = stack.copy();

            if (be.level != null && !be.level.isClientSide) {
                be.setStored(toInsert);
            }

            // 1) 先嘗試與可存取的所有槽位中「相同物品且可疊（忽略 creation_date）」的堆疊做 merge
            for (int i = 0; i < getSlots(); i++) {
                if (toInsert.isEmpty()) break;

                ItemStack existing = getStackInSlot(i);
                if (!existing.isEmpty()
                        && FoodCapability.areStacksStackableExceptCreationDate(existing, toInsert)) {

                    // 記錄修改前數量，方便計算實際移入幾個
                    int before = toInsert.getCount();

                    if (simulate) {
                        // 模擬：用副本計算
                        ItemStack existingCopy = existing.copy();
                        ItemStack toInsertCopy = toInsert.copy();
                        FoodCapability.mergeItemStacks(existingCopy, toInsertCopy);
                        // 計算在這個槽位能移入多少
                        int moved = before - toInsertCopy.getCount();
                        if (moved > 0) {
                            toInsert.shrink(moved);
                        }
                    } else {
                        FoodCapability.mergeItemStacks(existing, toInsert);
                        setStackInSlot(i, existing);
                        if (be.level != null && !be.level.isClientSide) {
                            be.setStored(existing);
                        }
                        be.setChanged();
                    }
                }
            }

            // 2) 若還有剩餘，嘗試放到可用的空槽位
            for (int i = 0; i < getSlots(); i++) {
                if (toInsert.isEmpty()) break;

                ItemStack existing = getStackInSlot(i);
                if (existing.isEmpty() && isItemValid(i, toInsert)) {
                    if (simulate) {
                        // 模擬：計算理論可放入量（受限於每槽上限與物品自身上限）
                        int limit = Math.min(getSlotLimit(i), toInsert.getMaxStackSize());
                        int moved = Math.min(limit, toInsert.getCount());
                        if (moved > 0) {
                            toInsert.shrink(moved);
                        }
                    } else {
                        // 實際：使用 FoodCapability.mergeItemStacks 讓 creation_date 規則一致
                        ItemStack newStack = FoodCapability.mergeItemStacks(ItemStack.EMPTY, toInsert);
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
}

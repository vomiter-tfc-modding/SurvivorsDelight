package com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle;

import com.mojang.datafixers.util.Pair;
import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotCommonMenu;
import com.vomiter.survivorsdelight.registry.SDContainerTypes;
import com.vomiter.survivorsdelight.mixin.device.cooking_pot.CookingPotBlockEntity_Accessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMealSlot;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotResultSlot;

import javax.annotation.Nullable;

public class SDCookingPotFluidMenu extends AbstractContainerMenu implements ICookingPotCommonMenu {
    public static final MenuType<SDCookingPotFluidMenu> TYPE = SDContainerTypes.POT_FLUID_MENU.get();
    public final BlockPos pos;
    private CookingPotBlockEntity pot;
    @Nullable private ICookingPotFluidAccess potFluidAccess;
    public static final int X_DEVIATION = 22;
    public static final int Y_DEVIATION = 1;
    private final ContainerData cookingPotData;

    private final DataSlot fluidAmount = DataSlot.standalone();
    private final DataSlot fluidCapacity = DataSlot.standalone();
    private int clientFluidCapacity = 4000;
    private FluidStack clientFluid = FluidStack.EMPTY;

    public SDCookingPotFluidMenu(int id, Inventory inv, FriendlyByteBuf buf) { this(id, inv, buf.readBlockPos()); }

    public SDCookingPotFluidMenu(int id, Inventory inv, BlockPos pos) {
        super(TYPE, id);
        this.pos = pos;

        final Level level = inv.player.level();
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CookingPotBlockEntity pot0) {
            this.pot = pot0;
            this.potFluidAccess = (ICookingPotFluidAccess) pot;
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
            if(handler != null){
                setClientFluid(handler.getFluidInTank(0));
                fluidCapacity.set(handler.getTankCapacity(0));
                fluidAmount.set(handler.getFluidInTank(0).getAmount());
            }
        }

        if (!level.isClientSide && pot != null) {
            this.cookingPotData = ((CookingPotBlockEntity_Accessor) pot).getCookingPotData(); // server
        } else {
            this.cookingPotData = new SimpleContainerData(2);
        }

        // === 綁定兩個「桶子 I/O」槽，直接連到方塊實體的 ItemStackHandler ===
        // slot index: 0 = input, 1 = output
        if (potFluidAccess != null) {
            var aux = potFluidAccess.sdtfc$getAuxInv();
            // 輸入：允許放入（驗證交由 ItemStackHandler#isItemValid），UI座標自行調整
            this.addSlot(new BucketInputSlot(aux, 0, 35 + X_DEVIATION, 20 + Y_DEVIATION));
            // 輸出：拒收放入，允許取出
            this.addSlot(new BucketOutputSlot(aux, 1, 35 + X_DEVIATION, 54 + Y_DEVIATION));
        } else {
            // 理論上不會發生；保底避免 NPE（給一個假的 1x1 容器）
            this.addSlot(new Slot(new SimpleContainer(1), 0, 35, 20));
            this.addSlot(new Slot(new SimpleContainer(1), 0, 35, 54));
        }

        this.addSlot(new CookingPotMealSlot(pot.getInventory() , 6, 124, 26));
        this.addSlot(new SlotItemHandler(pot.getInventory(), 7, 92, 55) {
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, CookingPotMenu.EMPTY_CONTAINER_SLOT_BOWL);
            }
        });
        this.addSlot(new CookingPotResultSlot(inv.player, pot, pot.getInventory(), 8, 124, 55));

        // === 玩家背包槽 ===
        final int yBase = 84;
        for (int row = 0; row < 3; ++row)
            for (int col = 0; col < 9; ++col)
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, yBase + row * 18));
        for (int col = 0; col < 9; ++col)
            this.addSlot(new Slot(inv, col, 8 + col * 18, yBase + 58));

        // === 用 DataSlot 同步「容量」 ===
        this.addDataSlot(new DataSlot() {
            @Override public int get() {
                if (potFluidAccess != null) return potFluidAccess.sdtfc$getTank().getCapacity();
                return 0;
            }
            @Override public void set(int value) {
                clientFluidCapacity = value;
            }
        });

        this.addDataSlots(this.cookingPotData);
    }

    public BlockPos getPos() { return pos; }

    @Override public boolean stillValid(@NotNull Player player) {
        return potFluidAccess != null && player.distanceToSqr(Vec3.atCenterOf(pos)) < 64.0;
    }

    public int getFluidCapacity() {
        return potFluidAccess != null ? potFluidAccess.sdtfc$getTank().getCapacity() : clientFluidCapacity;
    }

    public void setClientFluid(FluidStack fs) {
        this.clientFluid = fs.copy();
    }

    public FluidStack getClientFluid() { return clientFluid; }


    /**
     * 0..1: 我們的兩格（input=0, output=1）
     * 2..28: 玩家背包 (27 格)
     * 29..37: 玩家快捷列 (9 格)
     */
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack empty = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return empty;

        ItemStack stackInSlot = slot.getItem();
        ItemStack copy = stackInSlot.copy();

        final int AUX_START = 0;
        final int AUX_END_EXCL = 2; // 0..1
        final int INV_START = 2;
        final int INV_END_EXCL = 29; // 2..28
        final int HOTBAR_START = 29;
        final int HOTBAR_END_EXCL = 38; // 29..37

        if (index < AUX_END_EXCL) {
            // 來自 aux（input/output） -> 移到玩家背包+快捷列
            if (!this.moveItemStackTo(stackInSlot, INV_START, HOTBAR_END_EXCL, true)) return empty;
        } else {
            // 來自玩家背包/快捷列 -> 試圖放進 aux 的「輸入格」(slot 0)
            // 只嘗試輸入槽（輸出槽拒收）
            if (!this.moveItemStackTo(stackInSlot, AUX_START, AUX_START + 1, false)) {
                // 如果塞不進輸入槽，嘗試在背包/快捷列內部整理
                if (index < HOTBAR_START) {
                    // 背包 -> 快捷列
                    if (!this.moveItemStackTo(stackInSlot, HOTBAR_START, HOTBAR_END_EXCL, false)) return empty;
                } else {
                    // 快捷列 -> 背包
                    if (!this.moveItemStackTo(stackInSlot, INV_START, INV_END_EXCL, false)) return empty;
                }
            }
        }

        if (stackInSlot.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return copy;
    }

    @Override
    public CookingPotBlockEntity sdtfc$getBlockEntity() {
        return pot;
    }

    // --- 自訂 Slot：輸入/輸出 ---
    static class BucketInputSlot extends SlotItemHandler {
        public BucketInputSlot(ItemStackHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override public boolean mayPlace(@NotNull ItemStack stack) {
            return super.mayPlace(stack);
        }

        @Override public int getMaxStackSize() { return 1; }
    }

    static class BucketOutputSlot extends SlotItemHandler {
        public BucketOutputSlot(ItemStackHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }

        @Override public boolean mayPickup(@NotNull Player player) { return true; }
        @Override public int getMaxStackSize() { return 1; }
    }

    public int getCookProgressionScaled() {
        int i = this.cookingPotData.get(0);
        int j = this.cookingPotData.get(1);
        return j != 0 && i != 0 ? i * 24 / j : 0;
    }
}
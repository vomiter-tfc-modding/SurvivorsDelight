package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.CookingPotFluidIO;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import net.dries007.tfc.common.items.FluidContainerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public class CookingPotBlockEntity_InterfaceMixin  extends BlockEntity implements ICookingPotFluidAccess {
    // ====== Players To Send Pkt =======
    @Unique
    private final List<ServerPlayer> sdtfc$players = new ArrayList<>();

    public CookingPotBlockEntity_InterfaceMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override public List<ServerPlayer> sdtfc$getPlayer(){return sdtfc$players;}
    @Override public void sdtfc$addPlayer(ServerPlayer player){
        this.sdtfc$players.add(player);
    }
    @Override public void sdtfc$removePlayer(ServerPlayer player){
        this.sdtfc$players.remove(player);
    }

    // ====== Fluid Tank======
    @Unique private final FluidTank sdtfc$fluidTank = new FluidTank(4000) {
        @Override protected void onContentsChanged() { sdtfc$setChangedAndSync(); }
    };
    @Unique private final IFluidHandler sdtfc$fluidCap = sdtfc$fluidTank;
    @Unique @Override public FluidTank sdtfc$getTank() { return sdtfc$fluidTank; }
    // ====== Item Slot for buckets ======
    @Unique private final ItemStackHandler sdtfc$auxInv = new ItemStackHandler(2) {
        @Override protected void onContentsChanged(int slot) { sdtfc$setChangedAndSync(); }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // slot 0 = 輸入（允許桶/可裝流體的容器）；slot 1 = 輸出（拒收）
            if (slot == 1) return false;
            return stack.getItem() instanceof BucketItem
                    || stack.getItem() instanceof FluidContainerItem
                    || FluidUtil.getFluidHandler(stack).isPresent();
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
    @Unique @Override public ItemStackHandler sdtfc$getAuxInv() { return sdtfc$auxInv; }

    @Unique
    @Override
    public void sdtfc$updateFluidIOSlots() {
        if(level == null) return;
        if(level.getBlockEntity(getBlockPos()) instanceof CookingPotBlockEntity cookingPot){
            CookingPotFluidIO.updateFluidIOSlots(cookingPot);
        }
    }

    @Unique
    private void sdtfc$setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }


}

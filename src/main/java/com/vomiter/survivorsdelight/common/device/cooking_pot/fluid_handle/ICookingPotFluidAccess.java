package com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICookingPotFluidAccess {
    IFluidTank sdtfc$getTank();
    ItemStackHandler sdtfc$getAuxInv();
    void sdtfc$updateFluidIOSlots();
    void sdtfc$addPlayer(ServerPlayer player);
    void sdtfc$removePlayer(ServerPlayer player);
    List<ServerPlayer> sdtfc$getPlayer();

    default @Nullable IFluidHandler sd$getFluidHandler() {
        IFluidTank tank = sdtfc$getTank();
        return (tank instanceof IFluidHandler ih) ? ih : null;
    }


}

package com.vomiter.survivorsdelight.adapter.cooking_pot.fluid;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ICookingPotFluidAccess {
    IFluidTank sdtfc$getTank();
    ItemStackHandler sdtfc$getAuxInv();
    ItemStackHandler sdtfc$getInventory();
    void sdtfc$updateFluidIOSlots();
    void sdtfc$addPlayer(Player player);
    void sdtfc$removePlayer(Player player);
    List<Player> sdtfc$getPlayers();
    void sdtfc$setCheckNewRecipe(boolean b);

    default @Nullable IFluidHandler sd$getFluidHandler() {
        IFluidTank tank = sdtfc$getTank();
        return (tank instanceof IFluidHandler ih) ? ih : null;
    }


}

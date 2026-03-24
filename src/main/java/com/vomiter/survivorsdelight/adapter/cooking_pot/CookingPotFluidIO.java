package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotHasChanged;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.PotFluidSyncS2CPayload;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import java.util.Optional;

public class CookingPotFluidIO {
    public static void updateFluidIOSlots(CookingPotBlockEntity cookingPot) {
        var level = cookingPot.getLevel();
        if(level == null) return;
        var fluidAccess = (ICookingPotFluidAccess)cookingPot;
        var tank = fluidAccess.sdtfc$getTank();
        var inventory = fluidAccess.sdtfc$getAuxInv();
        final ItemStack input = fluidAccess.sdtfc$getAuxInv().getStackInSlot(0);
        if (!input.isEmpty() && fluidAccess.sdtfc$getAuxInv().getStackInSlot(1).isEmpty()) //only works when the input is not empty and output is empty
        {
            //Basically copied from barrel
            FluidHelpers.transferBetweenBlockEntityAndItem(input, cookingPot, level, cookingPot.getBlockPos(), (newOriginalStack, newContainerStack) -> {
                ((ICookingPotHasChanged)cookingPot).sdtfc$setChanged(true);
                //if(this instanceof LEGACY_ICookingPotRecipeBridge bridgePot) bridgePot.sdtfc$setCachedBridge(null);
                if (newContainerStack.isEmpty())
                {
                    // No new container was produced, so shove the first stack in the output, and clear the input
                    inventory.setStackInSlot(0, ItemStack.EMPTY);
                    inventory.setStackInSlot(1, newOriginalStack);
                }
                else
                {
                    // We produced a new container - this will be the 'filled', so we need to shove *that* in the output
                    inventory.setStackInSlot(0, newOriginalStack);
                    inventory.setStackInSlot(1, newContainerStack);
                }
                if(level.isClientSide) return;

                fluidAccess.sdtfc$getPlayer().forEach(player -> {
                    if(player.distanceToSqr(Vec3.atCenterOf(cookingPot.getBlockPos())) >= 64.0) fluidAccess.sdtfc$removePlayer(player);
                    else{
                        SDNetwork.sendToClient(
                                player,
                                new PotFluidSyncS2CPayload(cookingPot.getBlockPos(), Optional.of(BuiltInRegistries.FLUID.getKey(tank.getFluid().getFluid())), tank.getFluidAmount())
                        );

                    }
                });
            });
        }
    }

}

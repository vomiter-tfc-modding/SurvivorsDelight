package com.vomiter.survivorsdelight.adapter.cooking_pot.fluid;

import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.ICookingPotRecipeBridge;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.PotFluidSyncS2CPacket;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import java.util.List;

public class CookingPotFluidHandler {
    public static void updateFluidIOSlots(CookingPotBlockEntity pot){
        var self = (ICookingPotFluidAccess)pot;
        var tank = self.sdtfc$getTank();
        var inventory = self.sdtfc$getAuxInv();
        final ItemStack input = self.sdtfc$getAuxInv().getStackInSlot(0);
        if (!input.isEmpty() && self.sdtfc$getAuxInv().getStackInSlot(1).isEmpty()) //only works when the input is not empty and output is empty
        {
            //Basically copied from barrel
            assert pot.getLevel() != null;
            FluidHelpers.transferBetweenBlockEntityAndItem(input, pot, pot.getLevel(), pot.getBlockPos(), (newOriginalStack, newContainerStack) -> {
                self.sdtfc$setCheckNewRecipe(true); //to mimic inventory change
                if(pot instanceof ICookingPotRecipeBridge bridgePot) bridgePot.sdtfc$setCachedBridge(null); //to make it match pot recipe again
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
                if(pot.getLevel().isClientSide) return;

                List.copyOf(self.sdtfc$getPlayers()).forEach(player -> {
                    if(player.distanceToSqr(Vec3.atCenterOf(pot.getBlockPos())) >= 64.0) self.sdtfc$removePlayer(player);
                    else{
                        SDNetwork.CHANNEL.send( //send pack to make client redraw the fluid
                                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                                new PotFluidSyncS2CPacket(pot.getBlockPos(), ForgeRegistries.FLUIDS.getKey(tank.getFluid().getFluid()), tank.getFluidAmount())
                        );
                    }
                });
            });
        }

    }
}

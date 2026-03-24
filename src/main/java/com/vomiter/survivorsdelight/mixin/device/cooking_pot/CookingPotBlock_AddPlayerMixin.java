package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.PotFluidSyncS2CPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.CookingPotBlock;

import java.util.Optional;

@Mixin(CookingPotBlock.class)
public class CookingPotBlock_AddPlayerMixin {
    @Inject(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;openMenu(Lnet/minecraft/world/MenuProvider;Lnet/minecraft/core/BlockPos;)Ljava/util/OptionalInt;"))
    private void addPlayer(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<ItemInteractionResult> cir){
        var pot = (ICookingPotFluidAccess)level.getBlockEntity(pos);
        if(pot == null) return;
        var tank = pot.sdtfc$getTank();
        if(player instanceof ServerPlayer serverPlayer) {
            pot.sdtfc$addPlayer(serverPlayer);
            SDNetwork.sendToClient(
                    serverPlayer,
                    new PotFluidSyncS2CPayload(pos, Optional.of(BuiltInRegistries.FLUID.getKey(tank.getFluid().getFluid())), tank.getFluidAmount())
            );
        }
    }
}

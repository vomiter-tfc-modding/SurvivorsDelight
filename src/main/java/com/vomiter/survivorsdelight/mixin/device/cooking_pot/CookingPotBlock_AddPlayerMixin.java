package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.PotFluidSyncS2CPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.CookingPotBlock;

@Mixin(CookingPotBlock.class)
public class CookingPotBlock_AddPlayerMixin {
    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/network/NetworkHooks;openScreen(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/MenuProvider;Lnet/minecraft/core/BlockPos;)V"))
    private void addPlayer(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir){
        var pot = (ICookingPotFluidAccess)level.getBlockEntity(pos);
        if(pot == null) return;
        pot.sdtfc$addPlayer(player);
        var tank = pot.sdtfc$getTank();
        SDNetwork.CHANNEL.send(
                net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                new PotFluidSyncS2CPacket(pos, ForgeRegistries.FLUIDS.getKey(tank.getFluid().getFluid()), tank.getFluidAmount())
        );
    }
}

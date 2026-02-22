package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.SDCookingPotFluidMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import java.util.function.Supplier;

public record OpenBackToFDPotC2SPacket() {
    public static void encode(OpenBackToFDPotC2SPacket pkt, FriendlyByteBuf buf) {}
    public static OpenBackToFDPotC2SPacket decode(FriendlyByteBuf buf) { return new OpenBackToFDPotC2SPacket(); }
    public static void handle(OpenBackToFDPotC2SPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;
            if (sp.containerMenu instanceof SDCookingPotFluidMenu m) {
                BlockPos pos = m.getPos(); // 請在 PotFluidMenu 實作 public BlockPos getPos()
                BlockEntity be = sp.level().getBlockEntity(pos);
                if (be instanceof CookingPotBlockEntity pot) {
                    NetworkHooks.openScreen(sp, pot, pos);
                }
            }
        });
        c.setPacketHandled(true);
    }
}

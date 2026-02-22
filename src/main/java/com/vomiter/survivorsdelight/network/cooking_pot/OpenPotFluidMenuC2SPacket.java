package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.SDCookingPotFluidMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public record OpenPotFluidMenuC2SPacket(int containerId, BlockPos pos) {

    public static void encode(OpenPotFluidMenuC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.containerId());
        buf.writeBlockPos(pkt.pos());
    }

    public static OpenPotFluidMenuC2SPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        BlockPos pos = buf.readBlockPos();
        return new OpenPotFluidMenuC2SPacket(id, pos);
    }

    public static void handle(OpenPotFluidMenuC2SPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;

            // 防偽：確認封包的 containerId 與玩家當前開著的 menu 一致
            if (sp.containerMenu.containerId != pkt.containerId()) return;

            MenuProvider provider = new SimpleMenuProvider(
                    (windowId, inv, player) -> new SDCookingPotFluidMenu(windowId, inv, pkt.pos()),
                    Component.translatable("gui.survivorsdelight.pot.open_fluid")
            );
            NetworkHooks.openScreen(sp, provider, buf -> buf.writeBlockPos(pkt.pos()));
        });
        c.setPacketHandled(true);
    }
}

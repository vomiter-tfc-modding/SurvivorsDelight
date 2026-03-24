package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotFluidMenu;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

public record OpenBackToFDPotC2SPayload() implements CustomPacketPayload {

    public static final Type<OpenBackToFDPotC2SPayload> TYPE =
            new Type<>(SDUtils.RLUtils.build( "open_back_to_fd_pot"));

    public static final StreamCodec<FriendlyByteBuf, OpenBackToFDPotC2SPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenBackToFDPotC2SPayload());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenBackToFDPotC2SPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var sp = ctx.player();
            if (sp == null) return;
            if (sp.containerMenu instanceof SDCookingPotFluidMenu m) {
                BlockPos pos = m.getPos();
                BlockEntity be = sp.level().getBlockEntity(pos);
                if (be instanceof CookingPotBlockEntity pot) {
                    sp.openMenu(pot, buf -> buf.writeBlockPos(pos));
                }
            }
        });
    }
}

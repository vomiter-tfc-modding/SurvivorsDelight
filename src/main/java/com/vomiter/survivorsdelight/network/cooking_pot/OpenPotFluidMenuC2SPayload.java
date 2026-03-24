package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotFluidMenu;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record OpenPotFluidMenuC2SPayload(int containerId, BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenPotFluidMenuC2SPayload> TYPE =
            new Type<>(SDUtils.RLUtils.build("open_pot_fluid_menu"));

    public static final StreamCodec<FriendlyByteBuf, OpenPotFluidMenuC2SPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenPotFluidMenuC2SPayload::containerId,
                    BlockPos.STREAM_CODEC, OpenPotFluidMenuC2SPayload::pos,
                    OpenPotFluidMenuC2SPayload::new
            );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenPotFluidMenuC2SPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var sp = ctx.player();
            if (sp == null) return;

            if (sp.containerMenu.containerId != msg.containerId()) return;

            MenuProvider provider = new SimpleMenuProvider(
                    (windowId, inv, player) -> new SDCookingPotFluidMenu(windowId, inv, msg.pos()),
                    Component.translatable("gui.survivorsdelight.pot.open_fluid")
            );
            sp.openMenu(provider, buf -> buf.writeBlockPos(msg.pos()));
        });
    }
}

package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotFluidMenu;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record PotFluidSyncS2CPayload(BlockPos pos,
                                     Optional<ResourceLocation> fluidKey,
                                     int amount) implements CustomPacketPayload {

    public static final Type<PotFluidSyncS2CPayload> TYPE =
            new Type<>(SDUtils.RLUtils.build("pot_fluid_sync"));

    public static final StreamCodec<FriendlyByteBuf, PotFluidSyncS2CPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, PotFluidSyncS2CPayload::pos,
                    ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), PotFluidSyncS2CPayload::fluidKey,
                    ByteBufCodecs.VAR_INT, PotFluidSyncS2CPayload::amount,
                    PotFluidSyncS2CPayload::new
            );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(PotFluidSyncS2CPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            FluidStack stack = FluidStack.EMPTY;
            if (msg.fluidKey().isPresent()) {
                final Fluid f = mc.level.registryAccess()
                        .registryOrThrow(Registries.FLUID)
                        .get(msg.fluidKey().get());
                if (f != null) stack = new FluidStack(f, msg.amount());
            }

            if (mc.player.containerMenu instanceof SDCookingPotFluidMenu menu
                    && msg.pos().equals(menu.getPos())) {
                menu.setClientFluid(stack);
            }
        });
    }
}

package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.SDCookingPotFluidMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;


public record PotFluidSyncS2CPacket(BlockPos pos,
                                    @Nullable ResourceLocation fluidKey,
                                    int amount
) {

    // ---------- Encode / Decode ----------

    public static void encode(PotFluidSyncS2CPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos);
        // fluidKey 允許為 null，先寫個 boolean
        buf.writeBoolean(pkt.fluidKey != null);
        if (pkt.fluidKey != null) {
            buf.writeResourceLocation(pkt.fluidKey);
        }
        buf.writeVarInt(pkt.amount);
    }

    public static PotFluidSyncS2CPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        ResourceLocation key = null;
        if (buf.readBoolean()) {
            key = buf.readResourceLocation();
        }
        int amount = buf.readVarInt();
        return new PotFluidSyncS2CPacket(pos, key, amount);
    }

    // ---------- Handle on client ----------

    public static void handle(PotFluidSyncS2CPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        var c = ctx.get();
        c.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) return;

            // 用 vanilla RegistryAccess 解析 ResourceLocation → Fluid
            FluidStack stack = FluidStack.EMPTY;
            if (pkt.fluidKey != null) {
                Fluid f = mc.level.registryAccess()
                        .registryOrThrow(Registries.FLUID)
                        .get(pkt.fluidKey);
                if (f != null) stack = new FluidStack(f, pkt.amount);
            }

            // 只更新當前開啟、且 pos 相符的 PotFluidMenu
            if (mc.player.containerMenu instanceof SDCookingPotFluidMenu menu
                    && pkt.pos.equals(menu.getPos())) {
                menu.setClientFluid(stack);
            }
        });
        c.setPacketHandled(true);
    }
}
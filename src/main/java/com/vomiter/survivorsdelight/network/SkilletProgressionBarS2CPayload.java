package com.vomiter.survivorsdelight.network;

import com.vomiter.survivorsdelight.common.device.skillet.itemcooking.ISkilletItemCookingData;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import vectorwing.farmersdelight.common.item.SkilletItem;

public record SkilletProgressionBarS2CPayload(InteractionHand hand, int barWidth) implements CustomPacketPayload {
    public static final Type<SkilletProgressionBarS2CPayload> TYPE =
            new Type<>(SDUtils.RLUtils.build("skillet_progression_bar"));
    public static final StreamCodec<FriendlyByteBuf, SkilletProgressionBarS2CPayload> STREAM_CODEC =
            StreamCodec.of((buf, payload) -> {
                buf.writeEnum(payload.hand());
                buf.writeVarInt(payload.barWidth());
            }, buf -> new SkilletProgressionBarS2CPayload(buf.readEnum(InteractionHand.class), buf.readVarInt()));

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SkilletProgressionBarS2CPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            ItemStack stack = context.player().getItemInHand(payload.hand());
            if (stack.getItem() instanceof SkilletItem
                    && (Object) stack instanceof ISkilletItemCookingData data) {
                data.setCookingBarWidth(payload.barWidth());
            }
        });
    }
}

package com.vomiter.survivorsdelight.network;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletDeflects;
import com.vomiter.survivorsdelight.network.cooking_pot.ClearCookingPotMealC2SPayload;
import com.vomiter.survivorsdelight.network.cooking_pot.OpenBackToFDPotC2SPayload;
import com.vomiter.survivorsdelight.network.cooking_pot.OpenPotFluidMenuC2SPayload;
import com.vomiter.survivorsdelight.network.cooking_pot.PotFluidSyncS2CPayload;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * 1.21 NeoForge 網路註冊集中處理：
 * - 不再使用 SimpleChannel / NetworkRegistry。
 * - 在 RegisterPayloadHandlerEvent 中以 PayloadRegistrar 註冊雙向封包。
 * - 傳送改用 PacketDistributor。
 */
public final class SDNetwork {
    private SDNetwork() {}

    private static final String PROTOCOL = "1";

    /** 在主模組建構時，把這個方法用作 modBus 監聽器註冊即可。 */
    public static void onRegisterPayloads(final RegisterPayloadHandlersEvent event) {
        // 每個 mod 取一個 registrar，並設定協定版本
        final PayloadRegistrar registrar = event.registrar(SurvivorsDelight.MODID).versioned(PROTOCOL);

        // ---- 依序註冊你的封包 ----
        // 範例：改寫後的 SwingSkilletC2S（PLAY_TO_SERVER）
        registrar.playToServer(SwingSkilletC2S.TYPE, SwingSkilletC2S.STREAM_CODEC, SwingSkilletC2S::handle);

        registrar.playToClient(PotFluidSyncS2CPayload.TYPE, PotFluidSyncS2CPayload.STREAM_CODEC, PotFluidSyncS2CPayload::handle);
        registrar.playToServer(OpenPotFluidMenuC2SPayload.TYPE, OpenPotFluidMenuC2SPayload.STREAM_CODEC, OpenPotFluidMenuC2SPayload::handle);
        registrar.playToServer(OpenBackToFDPotC2SPayload.TYPE, OpenBackToFDPotC2SPayload.STREAM_CODEC, OpenBackToFDPotC2SPayload::handle);
        registrar.playToServer(ClearCookingPotMealC2SPayload.TYPE, ClearCookingPotMealC2SPayload.STREAM_CODEC, ClearCookingPotMealC2SPayload::handle);
    }

    /* ---------- 傳送輔助 ---------- */

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }
    public static void sendToClient(ServerPlayer player, CustomPacketPayload payload) {PacketDistributor.sendToPlayer(player, payload);}


    public record SwingSkilletC2S() implements CustomPacketPayload {
        public static final Type<SwingSkilletC2S> TYPE =
                new Type<>(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "swing_skillet"));
        public static final StreamCodec<FriendlyByteBuf, SwingSkilletC2S> STREAM_CODEC =
                StreamCodec.of(
                        // encoder：這包沒有內容，不寫任何資料
                        (buf, pkt) -> {},
                        // decoder：直接建空包
                        buf -> new SwingSkilletC2S()
                );

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void handle(final SwingSkilletC2S pkt, final IPayloadContext ctx) {
            ctx.enqueueWork(() -> {
                var player = ctx.player();
                if (player instanceof net.minecraft.server.level.ServerPlayer sp) {
                    SkilletDeflects.performSweepDeflect(sp);
                };
            });
        }
    }
}

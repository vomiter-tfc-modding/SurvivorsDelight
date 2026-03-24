package com.vomiter.survivorsdelight.mixin.debug;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.IdDispatchCodec;
import net.minecraft.network.codec.StreamCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IdDispatchCodec.class)
public abstract class IdDispatchCodec_DebugMixin<B extends ByteBuf, V> {
    /*

    @Inject(method = "decode", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;",
            shift = At.Shift.BEFORE
    ))
    private void sd$beforeDecode(B buf, CallbackInfoReturnable<V> cir) {
        // 這裡你可以暫存當前要用的 entry / type index
    }

    @Redirect(
            method = "decode",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/codec/StreamCodec;decode(Ljava/lang/Object;)Ljava/lang/Object;"
            )
    )
    private Object sd$wrapDecode(StreamCodec<B, ?> codec, Object bufObj) {
        B buf = (B) bufObj;
        try {
            return codec.decode(buf);
        } catch (Throwable t) {
            SurvivorsDelight.LOGGER.error(
                    "[IdDispatchCodec] decode 失敗，codec = {}", codec.getClass().getName(), t
            );
            throw t;
        }
    }
    /*
     */
}

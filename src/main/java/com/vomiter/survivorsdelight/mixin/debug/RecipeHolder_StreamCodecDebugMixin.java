package com.vomiter.survivorsdelight.mixin.debug;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeHolder.class)
public abstract class RecipeHolder_StreamCodecDebugMixin {

    /*
    @Shadow @Final @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<?>> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void sd$wrapRecipeHolderCodec(CallbackInfo ci) {
        final StreamCodec<RegistryFriendlyByteBuf, RecipeHolder<?>> original = STREAM_CODEC;

        STREAM_CODEC = new StreamCodec<>() {
            @Override
            public void encode(RegistryFriendlyByteBuf buf, RecipeHolder<?> holder) {
                ResourceLocation id = holder.id();
                Recipe<?> recipe = holder.value();
                RecipeSerializer<?> serializer = recipe.getSerializer();
                ResourceLocation serializerKey = getSerializerKey(serializer);

                int before = buf.writerIndex();
                original.encode(buf, holder);
                int after = buf.writerIndex();

                SurvivorsDelight.LOGGER.info(
                        "[RecipeHolderEncode] id={} type={} size={} bytes",
                        id, serializerKey, (after - before)
                );
            }

            @Override
            public RecipeHolder<?> decode(RegistryFriendlyByteBuf buf) {
                int before = buf.readerIndex();
                try {
                    RecipeHolder<?> holder = original.decode(buf);
                    int after = buf.readerIndex();

                    Recipe<?> recipe = holder.value();
                    RecipeSerializer<?> serializer = recipe.getSerializer();
                    ResourceLocation serializerKey = getSerializerKey(serializer);

                    SurvivorsDelight.LOGGER.info(
                            "[RecipeHolderDecode OK] id={} type={} size={} bytes",
                            holder.id(), serializerKey, (after - before)
                    );
                    return holder;
                } catch (Throwable t) {
                    SurvivorsDelight.LOGGER.error(
                            "[RecipeHolderDecode FAIL] readerIndexBefore={} remaining={} cause={}",
                            before, buf.readableBytes(), t.toString(), t
                    );
                    throw t;
                }
            }

            private ResourceLocation getSerializerKey(RecipeSerializer<?> serializer) {
                Registry<RecipeSerializer<?>> reg = BuiltInRegistries.RECIPE_SERIALIZER;
                ResourceLocation key = reg.getKey(serializer);
                return key != null ? key : ResourceLocation.fromNamespaceAndPath("unknown", serializer.getClass().getSimpleName());
            }
        };

        SurvivorsDelight.LOGGER.info("[SD-DEBUG] Wrapped RecipeHolder.STREAM_CODEC for debug");
    }
    /*
     */
}

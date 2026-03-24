package com.vomiter.survivorsdelight.registry.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record SDContainer(ResourceLocation itemId) {
    public static final Codec<SDContainer> CODEC =
            ResourceLocation.CODEC.xmap(SDContainer::new, SDContainer::itemId);

    public static final StreamCodec<FriendlyByteBuf, SDContainer> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC, SDContainer::itemId,
                    SDContainer::new
            );
}

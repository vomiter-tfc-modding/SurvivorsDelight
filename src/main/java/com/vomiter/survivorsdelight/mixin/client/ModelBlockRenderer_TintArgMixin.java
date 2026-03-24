package com.vomiter.survivorsdelight.mixin.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vomiter.survivorsdelight.client.tint.RottenFeastTint;
import com.vomiter.survivorsdelight.client.tint.TintingVertexConsumer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRenderer_TintArgMixin {

    @ModifyVariable(
            method = "putQuadData",
            at = @At("HEAD"),
            argsOnly = true
    )
    private VertexConsumer sd$wrapConsumerForRottenOverlay(
            VertexConsumer original,
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos
    ) {
        final int argb = RottenFeastTint.getOverlayColorIfShouldTint(level, pos, state);
        if (argb == 0) return original;

        final int r = (argb >> 16) & 0xFF;
        final int g = (argb >>  8) & 0xFF;
        final int b = (argb      ) & 0xFF;
        final int a = (argb >> 24) & 0xFF;
        return new TintingVertexConsumer(original, r, g, b, a);
    }
}

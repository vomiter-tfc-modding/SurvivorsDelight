package com.vomiter.survivorsdelight.client.tint;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public final class TintingVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int tr, tg, tb, ta;
    private final float fr, fg, fb; // 0..1

    public TintingVertexConsumer(VertexConsumer delegate, int r, int g, int b, int a) {
        this.delegate = delegate;
        this.tr = r; this.tg = g; this.tb = b; this.ta = a;
        this.fr = r / 255f; this.fg = g / 255f; this.fb = b / 255f;
    }

    @Override
    public void putBulkData(PoseStack.@NotNull Pose pose, @NotNull BakedQuad quad,
                            float @NotNull [] brightness, float r, float g, float b, float a,
                            int @NotNull [] lights, int overlay, boolean useShade) {
        delegate.putBulkData(pose, quad, brightness, r * fr, g * fg, b * fb, a, lights, overlay, useShade);
    }

    @Override
    public void putBulkData(PoseStack.@NotNull Pose pose, @NotNull BakedQuad quad,
                            float r, float g, float b, float a, int light, int overlay) {
        delegate.putBulkData(pose, quad, r * fr, g * fg, b * fb, a, light, overlay);
    }

    @Override
    public @NotNull VertexConsumer addVertex(float v, float v1, float v2) {
        return delegate.addVertex(v, v1, v2);
    }

    @Override
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
        int nr = (r * tr) / 255;
        int ng = (g * tg) / 255;
        int nb = (b * tb) / 255;
        int na = (a * ta) / 255;
        return delegate.setColor(nr, ng, nb, na);
    }

    @Override
    public @NotNull VertexConsumer addVertex(@NotNull Matrix4f matrix, float x, float y, float z) { return delegate.addVertex(matrix, x, y, z); }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) { return delegate.setUv(u, v); }

    @Override
    public @NotNull VertexConsumer setUv1(int i, int i1) {
        return delegate.setUv1(i, i1);
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        return delegate.setUv2(u, v);
    }

    @Override
    public @NotNull VertexConsumer setNormal(float v, float v1, float v2) {
        return delegate.setNormal(v, v1, v2);
    }


}
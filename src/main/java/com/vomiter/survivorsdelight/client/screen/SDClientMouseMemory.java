package com.vomiter.survivorsdelight.client.screen;

import net.minecraft.client.Minecraft;

public final class SDClientMouseMemory {
    private static Double lastGuiX, lastGuiY;

    public static void rememberCurrent() {
        var mc = Minecraft.getInstance();
        var window = mc.getWindow();
        // 把實際視窗座標換算成 GUI 座標（scaled）
        double guiX = mc.mouseHandler.xpos() * window.getGuiScaledWidth() / (double) window.getWidth();
        double guiY = mc.mouseHandler.ypos() * window.getGuiScaledHeight() / (double) window.getHeight();
        lastGuiX = guiX;
        lastGuiY = guiY;
    }

    public static void restoreAndClear() {
        if (lastGuiX == null || lastGuiY == null) return;

        var mc = Minecraft.getInstance();
        var window = mc.getWindow();

        double winX = lastGuiX * window.getWidth() / (double) window.getGuiScaledWidth();
        double winY = lastGuiY * window.getHeight() / (double) window.getGuiScaledHeight();

        org.lwjgl.glfw.GLFW.glfwSetCursorPos(window.getWindow(), winX, winY);
        lastGuiX = lastGuiY = null;
    }
}

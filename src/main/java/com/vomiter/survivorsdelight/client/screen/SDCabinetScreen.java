package com.vomiter.survivorsdelight.client.screen;
// SDCabinetScreen.java

import com.mojang.blaze3d.systems.RenderSystem;
import com.vomiter.survivorsdelight.common.container.SDCabinetMenu;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class SDCabinetScreen extends AbstractContainerScreen<SDCabinetMenu> implements MenuAccess<SDCabinetMenu> {
    private static final ResourceLocation TEXTURE = SDUtils.RLUtils.build("minecraft", "textures/gui/container/generic_54.png");

    private final int rows;

    public SDCabinetScreen(SDCabinetMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.rows = menu.getRows();                         // 確保 Menu 有 public int getRows()
        this.imageWidth = 176;                              // 原版 chest 寬度固定 176
        this.imageHeight = 114 + this.rows * 18;            // 114（頭+玩家背包）+ 每排 18
        this.inventoryLabelY = this.imageHeight - 94;       // 玩家背包標題 y（同 ChestScreen）
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos  = (this.height - this.imageHeight) / 2;
    }

    @Override
    public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gg, mouseX, mouseY, partialTicks);
        super.render(gg, mouseX, mouseY, partialTicks);
        this.renderTooltip(gg, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, TEXTURE);

        // 兩段式貼圖：上半部（容器格）+ 下半部（玩家背包）
        // 上半部：高度 = rows*18 + 17
        int x = this.leftPos;
        int y = this.topPos;
        int topHeight = 17 + this.rows * 18;

        // (u,v) 從 0,0 開始
        gg.blit(TEXTURE, x, y, 0, 0, this.imageWidth, topHeight);

        // 下半部（固定 96 高，背包+快捷欄）
        gg.blit(TEXTURE, x, y + topHeight, 0, 126, this.imageWidth, 96);
    }
}

package com.vomiter.survivorsdelight.client.screen;

import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.OpenBackToFDPotC2SPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModItems;

public class SDBackToPotButton extends AbstractButton {

    public SDBackToPotButton(int x, int y, Component tooltip) {
        super(x, y, 16, 16, Component.empty());
        // 固定 tooltip 直接交給內建系統處理
        this.setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress() {
        // 1.21 payload 路線
        SDNetwork.sendToServer(new OpenBackToFDPotC2SPayload());
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        // hover 邊框
        if (this.isHoveredOrFocused()) {
            gg.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, 0x80FFFFFF);
        }

        // 圖示：FD 的鍋
        final int x = getX();
        final int y = getY() + (height - 16) / 2;
        Item cookingPot = ModItems.COOKING_POT.get();
        gg.renderItem(cookingPot.getDefaultInstance(), x, y);
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
        // 有 setTooltip 時，螢幕閱讀器會自動處理基本敘述；必要時再補充
    }
}

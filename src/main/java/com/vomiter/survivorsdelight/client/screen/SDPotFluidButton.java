package com.vomiter.survivorsdelight.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.OpenPotFluidMenuC2SPayload;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.ArrayList;
import java.util.List;

public class SDPotFluidButton extends AbstractButton {
    private final CookingPotMenu menu;
    private final Component basicTooltip;

    public SDPotFluidButton(int x, int y, Component tooltip, CookingPotMenu menu) {
        super(x, y, 16, 16, Component.empty());
        this.menu = menu;
        this.basicTooltip = tooltip;
    }

    @Override
    public void onPress() {
        SDClientMouseMemory.rememberCurrent();          // 先記目前滑鼠
        SDNetwork.sendToServer(new OpenPotFluidMenuC2SPayload(this.menu.containerId, this.menu.blockEntity.getBlockPos()));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        // hover 邊框
        if (this.isHoveredOrFocused()) {
            gg.fill(getX() - 1, getY() - 1, getX() + width + 1, getY() + height + 1, 0x80FFFFFF);
        }

        // 內容繪製
        FluidStack stack = ((ICookingPotFluidAccess) menu.blockEntity).sdtfc$getTank().getFluid();
        int x = getX();
        int y = getY() + (height - 16) / 2;

        if (!stack.isEmpty()) {
            final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(stack);
            gg.blit(x, y, 0, 16, 16, sprite);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } else {
            Item bucket = TFCItems.WOODEN_BUCKET.get();
            gg.renderItem(bucket.getDefaultInstance(), x, y);
        }

        // 動態 tooltip（放在這裡，不要覆寫 final 的 render）
        if (this.isHoveredOrFocused()) {
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(basicTooltip);
            if (!stack.isEmpty()) {
                // getDisplayName() 已棄用 → 改用 getHoverName()
                tooltips.add(Component.literal(stack.getHoverName().getString() + " " + stack.getAmount() + "/4000"));
            }
            gg.renderComponentTooltip(Minecraft.getInstance().font, tooltips, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
        // 可依需要補上敘述內容
    }
}

package com.vomiter.survivorsdelight.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.OpenPotFluidMenuC2SPacket;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.util.ArrayList;
import java.util.List;

public class SDPotFluidButton extends AbstractButton {
    private final CookingPotMenu menu;
    private final Component basicTooltip;

    public SDPotFluidButton(int x, int y,
                            Component tooltip,
                            CookingPotMenu menu
    ) {
        super(x, y, 16, 16, Component.empty());
        this.menu = menu;
        basicTooltip = tooltip;
    }

    @Override
    public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        super.render(gg, mouseX, mouseY, partialTick);
        List<Component> tooltips = new ArrayList<>();
        tooltips.add(basicTooltip);
        FluidStack stack = ((ICookingPotFluidAccess)menu.blockEntity).sdtfc$getTank().getFluid();
        if(!stack.isEmpty()){
            tooltips.add(Component.literal(stack.getDisplayName().getString() + " " + stack.getAmount() + "/4000"));
        }

        if(this.isHoveredOrFocused()){
            gg.renderComponentTooltip(Minecraft.getInstance().font, tooltips, mouseX, mouseY);
        }
    }

    @Override
    public void onPress() {
        SDNetwork.CHANNEL.sendToServer(new OpenPotFluidMenuC2SPacket(this.menu.containerId, this.menu.blockEntity.getBlockPos()));
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partialTick) {
        if (this.isHoveredOrFocused()) {
            gg.fill(getX()-1, getY()-1, getX()+width+1, getY()+height+1, 0x80FFFFFF);
        }
        FluidStack stack = ((ICookingPotFluidAccess)menu.blockEntity).sdtfc$getTank().getFluid();
        int x = getX();
        int y = getY() + (height - 16) / 2;
        if(!stack.isEmpty()){
            final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(stack);
            gg.blit(x, y, 0, 16, 16, sprite);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        } else{
            Item bucket = TFCItems.WOODEN_BUCKET.get();
            gg.renderItem(bucket.getDefaultInstance(), x, y);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput p_259858_) {

    }
}

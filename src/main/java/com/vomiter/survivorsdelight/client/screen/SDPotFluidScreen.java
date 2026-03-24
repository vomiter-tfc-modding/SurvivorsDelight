package com.vomiter.survivorsdelight.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotFluidMenu;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.ClearCookingPotMealC2SPayload;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.util.tooltip.Tooltips;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Unique;

import java.awt.*;

public class SDPotFluidScreen extends AbstractContainerScreen<SDCookingPotFluidMenu> {
    private static final ResourceLocation BG = SDUtils.RLUtils.build("survivorsdelight", "textures/gui/pot_fluid.png");
    private static final Rectangle PROGRESS_ARROW = new Rectangle(89, 25, 0, 17);
    private static final ResourceLocation COOKING_POT_BG = SDUtils.RLUtils.build("farmersdelight", "textures/gui/cooking_pot.png");
    public static final int X_DEVIATION = 22;
    public static final int Y_DEVIATION = 1;

    @Unique
    private int sdtfc$recipeBtnX() { return this.leftPos + 5; }
    @Unique private int sdtfc$recipeBtnY() { return this.height / 2 - 49; }

    @Unique private int sdtfc$bucketX() { return sdtfc$recipeBtnX() + 2; }
    @Unique private int sdtfc$bucketY() { return sdtfc$recipeBtnY() - 20; }

    public SDPotFluidScreen(SDCookingPotFluidMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override protected void init() {
        super.init();
        SDBackToPotButton potButton = new SDBackToPotButton(
                sdtfc$bucketX(), sdtfc$bucketY(), Component.translatable("gui.survivorsdelight.pot.pot_menu")
        );
        this.addRenderableWidget(potButton);
        SDClientMouseMemory.restoreAndClear(); // 把游標移回之前的 GUI 位置
    }

    @Override public void render(@NotNull GuiGraphics gg, int mouseX, int mouseY, float partial) {
        renderBackground(gg, mouseX, mouseY, partial);
        super.render(gg, mouseX, mouseY, partial);
        renderTooltip(gg, mouseX, mouseY);
    }

    @Override protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        graphics.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int cap = menu.getFluidCapacity();
        FluidStack stack = menu.getClientFluid();
        var l = menu.getCookProgressionScaled();
        graphics.blit(COOKING_POT_BG, this.leftPos + PROGRESS_ARROW.x, this.topPos + PROGRESS_ARROW.y, 176, 15, l + 1, PROGRESS_ARROW.height);


        if (!stack.isEmpty() && cap > 0 && stack.getAmount() > 0) {
            final TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(stack);
            final int fillHeight = (int) Math.ceil((float) 50 * stack.getAmount() / 4000f);
            RenderHelpers.fillAreaWithSprite(graphics, sprite, leftPos + 8 + X_DEVIATION, topPos + 70 +Y_DEVIATION - fillHeight, 16, fillHeight, 16, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }
    }

    @Override protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY){
        int relX = mouseX - this.getGuiLeft();
        int relY = mouseY - this.getGuiTop();
        if (
                relX >= 7 + X_DEVIATION
                        && relY >= 19 +Y_DEVIATION
                        && relX  < 25 +X_DEVIATION
                        && relY < 71 + Y_DEVIATION) {
            FluidStack stack = menu.getClientFluid();
            graphics.renderTooltip(this.font, Tooltips.fluidUnitsOf(stack), mouseX, mouseY);
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int mouseX, int mouseY, @NotNull ClickType clickType) {
        if (slot != null && slot.index == 2 && clickType == ClickType.PICKUP) {
            ItemStack carried = this.getMenu().getCarried();
            if (sdtfc$isWaterBucket(carried)) {
                SDNetwork.sendToServer(new ClearCookingPotMealC2SPayload(this.menu.sdtfc$getBlockEntity().getBlockPos()));
                return;
            }
        }

        super.slotClicked(slot, mouseX, mouseY, clickType);
    }

    private static boolean sdtfc$isWaterBucket(ItemStack stack) {
        IFluidHandler fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if(fluidHandler != null && fluidHandler.getFluidInTank(0).getFluid().isSame(Fluids.WATER)){
            fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }


}

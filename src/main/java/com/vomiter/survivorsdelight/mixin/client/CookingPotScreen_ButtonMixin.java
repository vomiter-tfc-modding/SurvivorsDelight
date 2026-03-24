package com.vomiter.survivorsdelight.mixin.client;

import com.vomiter.survivorsdelight.client.screen.SDPotFluidButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

import java.awt.*;

@Mixin(value = CookingPotScreen.class, remap = false)
public abstract class CookingPotScreen_ButtonMixin extends AbstractContainerScreen<CookingPotMenu> {
    @Shadow @Final @Mutable private static Rectangle HEAT_ICON;
    @Unique private SDPotFluidButton sdtfc$fluidButton;

    @Unique private int sdtfc$recipeBtnX() { return this.leftPos + 5; }
    @Unique private int sdtfc$recipeBtnY() { return this.height / 2 - 49; }

    @Unique private int sdtfc$bucketX() { return sdtfc$recipeBtnX() + 2; }
    @Unique private int sdtfc$bucketY() { return sdtfc$recipeBtnY() - 20; }

    public CookingPotScreen_ButtonMixin(CookingPotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Inject(method = "init", at = @At("TAIL"), remap = true)
    private void sd$addBucketBtn(CallbackInfo ci) {
        sdtfc$fluidButton = new SDPotFluidButton(
                sdtfc$bucketX(),
                sdtfc$bucketY(),
                Component.translatable("gui.survivorsdelight.pot.fluid_menu"),
                this.menu
        );
        this.addRenderableWidget(sdtfc$fluidButton);
    }

    @Inject(
            method = "lambda$init$0(Lnet/minecraft/client/gui/components/Button;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;setPosition(II)V", shift = At.Shift.AFTER),
            remap = true
    )
    private void foo(Button button, CallbackInfo ci){
        if(sdtfc$fluidButton != null) sdtfc$fluidButton.setPosition(sdtfc$bucketX(), sdtfc$bucketY());
    }
}

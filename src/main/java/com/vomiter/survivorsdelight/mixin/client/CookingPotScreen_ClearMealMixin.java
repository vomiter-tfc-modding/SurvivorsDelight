package com.vomiter.survivorsdelight.mixin.client;

import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.network.cooking_pot.ClearCookingPotMealC2SPayload;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.client.gui.CookingPotScreen;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

@Mixin(CookingPotScreen.class)
public abstract class CookingPotScreen_ClearMealMixin extends AbstractContainerScreen<CookingPotMenu> {
    public CookingPotScreen_ClearMealMixin(CookingPotMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    protected void slotClicked(Slot slot, int mouseX, int mouseY, ClickType clickType, CallbackInfo ci) {
        if (slot != null && slot.index == 6 && clickType == ClickType.PICKUP) {
            ItemStack carried = this.getMenu().getCarried();
            if (sdtfc$isWaterBucket(carried)) {
                SDNetwork.sendToServer(new ClearCookingPotMealC2SPayload(this.menu.blockEntity.getBlockPos()));
                ci.cancel();
            }
        }
    }

    @Unique
    private static boolean sdtfc$isWaterBucket(ItemStack stack) {
        IFluidHandler fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if(fluidHandler != null && fluidHandler.getFluidInTank(0).getFluid().isSame(Fluids.WATER)){
            fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }
}

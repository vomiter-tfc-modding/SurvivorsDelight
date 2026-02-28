package com.vomiter.survivorsdelight.mixin.compat.firmalife;

import com.eerussianguy.firmalife.compat.jei.FLJEIPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModItems;

@Mixin(value = FLJEIPlugin.class, remap = false)
public class FLJEIPluginMixin {
    @Inject(method = "registerRecipeCatalysts", at = @At("TAIL"))
    private void addCookingPot(IRecipeCatalystRegistration r, CallbackInfo ci){
        r.addRecipeCatalyst(new ItemStack(ModItems.COOKING_POT.get()), FLJEIPlugin.BOWL_POT);
    }
}

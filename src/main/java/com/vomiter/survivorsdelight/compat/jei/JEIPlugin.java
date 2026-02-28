package com.vomiter.survivorsdelight.compat.jei;

import com.vomiter.survivorsdelight.util.SDUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.dries007.tfc.compat.jei.JEIIntegration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModItems;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return SDUtils.RLUtils.build("jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.COOKING_POT.get()), JEIIntegration.SOUP_POT);
        registration.addRecipeCatalyst(new ItemStack(ModItems.COOKING_POT.get()), JEIIntegration.SIMPLE_POT);
    }
}

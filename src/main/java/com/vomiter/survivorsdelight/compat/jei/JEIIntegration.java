package com.vomiter.survivorsdelight.compat.jei;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class JEIIntegration implements IModPlugin
{
    private static IJeiRuntime runtime;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(SurvivorsDelight.MODID, "jei_plugin");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
    }

    public static boolean isJEIScreen() {
        if (runtime != null) {
            try {
                // JEI's API might throw an exception if the screen is not a JEI screen.
                return false;
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }
}

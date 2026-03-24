package com.vomiter.survivorsdelight.compat.jei;

import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.Minecraft;

public class JEIHelpers {
    public static boolean isInJEIScreen(){
        return Minecraft.getInstance().screen instanceof IRecipesGui;
    }
}

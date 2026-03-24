package com.vomiter.survivorsdelight.common.device.cutting;

import net.minecraft.world.item.ItemStack;

public final class CuttingContext {
    private static final ThreadLocal<ItemStack> CURRENT_INPUT = new ThreadLocal<>();

    public static void set(ItemStack input) { CURRENT_INPUT.set(input); }
    public static ItemStack get() { return CURRENT_INPUT.get(); }
    public static void clear() { CURRENT_INPUT.remove(); }
    private CuttingContext() {}
}
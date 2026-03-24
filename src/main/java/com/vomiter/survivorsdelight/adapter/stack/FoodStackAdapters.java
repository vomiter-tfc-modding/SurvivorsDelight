package com.vomiter.survivorsdelight.adapter.stack;

import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

// TFC

public final class FoodStackAdapters {
    private FoodStackAdapters() {}

    /** TFC 規則：忽略 creation_date 判斷是否可疊 */
    public static boolean stackableExceptCreationDate(@NotNull ItemStack a, @NotNull ItemStack b) {
        return FoodCapability.areStacksStackableExceptCreationDate(a, b);
    }

    /**
     * TFC 規則：把 src 併入 dst，並回傳「剩餘的 src」。
     * 注意：TFC 的 mergeItemStacks 會直接 mutate 參數，因此這裡只包裝語意。
     */
    public static @NotNull ItemStack mergeInto(@NotNull ItemStack dst, @NotNull ItemStack src) {
        return FoodCapability.mergeItemStacks(dst, src);
    }

    /**
     * simulate 用：計算把 src 併入 dst 後，src 會減少多少（moved count）
     */
    public static int simulateMovedCount(@NotNull ItemStack dst, @NotNull ItemStack src) {
        if (dst.isEmpty() && src.isEmpty()) return 0;
        int before = src.getCount();

        ItemStack dstCopy = dst.copy();
        ItemStack srcCopy = src.copy();
        FoodCapability.mergeItemStacks(dstCopy, srcCopy);

        return before - srcCopy.getCount();
    }
}

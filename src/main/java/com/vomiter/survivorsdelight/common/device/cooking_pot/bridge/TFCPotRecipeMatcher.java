package com.vomiter.survivorsdelight.common.device.cooking_pot.bridge;

import net.dries007.tfc.common.recipes.PotRecipe;
import net.dries007.tfc.common.recipes.TFCRecipeTypes;
import net.dries007.tfc.util.Helpers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TFCPotRecipeMatcher {

    private TFCPotRecipeMatcher() {}

    /**
     * 在任何容器上嘗試找出第一個匹配的 TFC PotRecipe。
     *
     * @param level              伺服端世界
     * @param items              物品槽
     * @param fluids             液體槽（至少要能讀到第 0 槽）
     * @param ingredientSlots    指定哪些 item slot 視為「鍋子的原料槽」（對應 TFC 的 4..8）
     */
    public static Optional<PotRecipe> findFirstMatch(
            Level level,
            IItemHandler items,
            IFluidHandler fluids,
            int[] ingredientSlots
    ) {
        // 1) 蒐集候選配方
        final var recipes = level.getRecipeManager().getAllRecipesFor(TFCRecipeTypes.POT.get());
        if (recipes.isEmpty()) return Optional.empty();

        // 2) 讀容器內容
        final FluidStack fluidInTank0 = fluids.getTanks() > 0 ? fluids.getFluidInTank(0) : FluidStack.EMPTY;
        final List<ItemStack> stacks = new ArrayList<>();
        for (int slot : ingredientSlots) {
            ItemStack s = items.getStackInSlot(slot);
            if (!s.isEmpty()) stacks.add(s);
        }

        // 3) 逐一比對：fluid・items
        for (RecipeHolder<PotRecipe> rh : recipes) {
            var r = rh.value();
            // 3a) 先比對流體
            final SizedFluidIngredient needed = r.getFluidIngredient();
            if (!needed.test(fluidInTank0)) continue;

            // 3b) 再比對原料（完全配對）
            if (!matchesItemsExactly(stacks, r.getItemIngredients())) continue;

            return Optional.of(r);
        }
        return Optional.empty();
    }

    private static boolean matchesItemsExactly(List<ItemStack> present, List<Ingredient> needed) {
        if (present.isEmpty() && needed.isEmpty()) return true;
        return Helpers.perfectMatchExists(present, needed);
    }
}


package com.vomiter.survivorsdelight.mixin.recipe.cooking;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotRecipeFluidAccess;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import javax.annotation.Nullable;

@Mixin(CookingPotRecipe.class)
public abstract class CookingPotRecipe_FluidDuckMixin implements IFluidRequiringRecipe {

    @Shadow @Final private NonNullList<Ingredient> inputItems;
    @Unique @Nullable private FluidStackIngredient sdtfc$fluidReq;
    @Unique private int sdtfc$fluidAmount;

    // --- IFluidRequiringRecipe ---
    @Override public @Nullable FluidStackIngredient sdtfc$getFluidIngredient() { return sdtfc$fluidReq; }
    @Override public int sdtfc$getRequiredFluidAmount() { return sdtfc$fluidAmount; }
    @Override public void sdtfc$setFluidRequirement(@Nullable FluidStackIngredient ing, int amount) {
        this.sdtfc$fluidReq = ing;
        this.sdtfc$fluidAmount = Math.max(0, amount);
    }

    /**
     * 原本 items 比對成功後，若本配方有 fluid 要求，就再檢查一次槽內流體。
     * 不破壞舊行為（舊配方不含 fluid 欄位時不會進來）。
     */
    @Inject(
            method = "matches(Lnet/minecraftforge/items/wrapper/RecipeWrapper;Lnet/minecraft/world/level/Level;)Z",
            at = @At("RETURN"),
            cancellable = true,
            remap = false
    )
    private void sdtfc$appendFluidCheck(RecipeWrapper inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return; // 物品已經不匹配就不檢查了
        if (sdtfc$fluidReq == null || sdtfc$fluidAmount <= 0) return; // 沒定義 fluid 要求
        if (!(inv instanceof ICookingPotRecipeFluidAccess fa)) {
            cir.setReturnValue(false);
            return;
        }
        FluidStack inTank = fa.getFluidInTank();
        if (inTank.isEmpty() || inTank.getAmount() < sdtfc$fluidAmount) {
            cir.setReturnValue(false);
            return;
        }
        if (!sdtfc$fluidReq.test(inTank)) {
            cir.setReturnValue(false);
        }
    }
}

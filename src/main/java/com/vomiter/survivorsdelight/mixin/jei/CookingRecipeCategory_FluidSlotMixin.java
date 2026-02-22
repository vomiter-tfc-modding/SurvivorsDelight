package com.vomiter.survivorsdelight.mixin.jei;

import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.dries007.tfc.compat.jei.category.BaseRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.integration.jei.category.CookingRecipeCategory;

import java.util.List;

@Mixin(value = CookingRecipeCategory.class, remap = false)
public abstract class CookingRecipeCategory_FluidSlotMixin {

    @Unique
    private static final int SD_FLUID_X = 0;
    @Unique
    private static final int SD_FLUID_Y = 36;

    @Inject(method = "setRecipe*", at = @At("TAIL"))
    private void sdtfc$addFluidSlot(IRecipeLayoutBuilder builder, CookingPotRecipe recipe, IFocusGroup focus, CallbackInfo ci) {
        FluidStackIngredient ingredient = ((IFluidRequiringRecipe)recipe).sdtfc$getFluidIngredient();
        if(ingredient == null) return;
        List<FluidStack> fluids = BaseRecipeCategory.collapse(ingredient);

        if (!fluids.isEmpty()) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, SD_FLUID_X, SD_FLUID_Y);
            slot.setFluidRenderer(1, false, 16, 16);
            slot.addIngredients(ForgeTypes.FLUID_STACK, fluids);
            slot.addTooltipCallback((view, tooltip) -> view.getDisplayedIngredient(ForgeTypes.FLUID_STACK).ifPresent(fs -> {
                tooltip.add(Component.translatable("tooltip.survivorsdelight.fluid_required"));
                tooltip.add(Component.literal(fs.getDisplayName().getString() + " · " + fs.getAmount() + " mB"));
            }));
        }
    }
}

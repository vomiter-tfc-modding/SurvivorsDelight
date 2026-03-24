package com.vomiter.survivorsdelight.mixin.jei;

import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.dries007.tfc.compat.jei.category.BaseRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
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

    @Inject(method = "setRecipe(Lmezz/jei/api/gui/builder/IRecipeLayoutBuilder;Lnet/minecraft/world/item/crafting/RecipeHolder;Lmezz/jei/api/recipe/IFocusGroup;)V", at = @At("TAIL"))
    private void sdtfc$addFluidSlot(IRecipeLayoutBuilder builder, RecipeHolder<CookingPotRecipe> holder, IFocusGroup focusGroup, CallbackInfo ci) {
        if(!(holder.value() instanceof SDCookingPotRecipe sdCookingPotRecipe)) return;
        int fluidAmount = sdCookingPotRecipe.getFluidAmountMb();
        if(fluidAmount <= 0) return;
        FluidIngredient fluidIngredient = sdCookingPotRecipe.getFluid();
        if(fluidIngredient == null) return;
        SizedFluidIngredient ingredient = new SizedFluidIngredient(fluidIngredient, fluidAmount);
        List<FluidStack> fluids = BaseRecipeCategory.collapse(ingredient);

        if (!fluids.isEmpty()) {
            var slot = builder.addSlot(RecipeIngredientRole.INPUT, SD_FLUID_X, SD_FLUID_Y);
            slot.setFluidRenderer(1, false, 16, 16);
            slot.addIngredients(NeoForgeTypes.FLUID_STACK, fluids);
            slot.addRichTooltipCallback((view, tooltip) -> view.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).ifPresent(fs -> {
                tooltip.add(Component.translatable("tooltip.survivorsdelight.fluid_required"));
                tooltip.add(Component.literal(fs.getHoverName().getString() + " · " + fs.getAmount() + " mB"));
            }));
        }
    }


}

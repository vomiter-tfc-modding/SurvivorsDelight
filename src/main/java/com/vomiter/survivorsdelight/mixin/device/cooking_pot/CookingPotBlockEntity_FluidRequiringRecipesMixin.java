package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotCalcDynamic;
import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotHasChanged;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.common.device.cooking_pot.wrap.CookingPotFluidRecipeWrapper;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.Objects;

@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntity_FluidRequiringRecipesMixin extends SyncedBlockEntity implements ICookingPotCalcDynamic {

    @Unique private ItemStack sdtfc$cachedResult = ItemStack.EMPTY;
    public CookingPotBlockEntity_FluidRequiringRecipesMixin(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public ItemStack sdtfc$getCachedDynamic(){return sdtfc$cachedResult;}
    public void sdtfc$setCachedDynamic(ItemStack stack){sdtfc$cachedResult = stack;}

    @Final @Shadow private ItemStackHandler inventory;

    @ModifyVariable(
            method = "getMatchingRecipe",
            at = @At("HEAD"),
            argsOnly = true
    )
    private RecipeWrapper wrapWithFluid(RecipeWrapper original) {
        if (!(this instanceof ICookingPotFluidAccess acc)) {
            return original;
        }

        var tankHandler = acc.sd$getFluidHandler();
        FluidStack snapshot = FluidStack.EMPTY;
        if (tankHandler != null && tankHandler.getTanks() > 0) {
            snapshot = tankHandler.getFluidInTank(0).copy();
        }

        return new CookingPotFluidRecipeWrapper(this.inventory, snapshot);
    }

    @Inject(method = "canCook", at = @At(value = "RETURN"), cancellable = true)
    private void checkResultStackable(CookingPotRecipe recipe, CallbackInfoReturnable<Boolean> cir){
        if(!cir.getReturnValue()) return;
        if(!(recipe instanceof SDCookingPotRecipe sdRecipe)) return;
        if(!sdRecipe.shouldCalcDynamic()) return;
        if(inventory.getStackInSlot(6).isEmpty()) return;
        if(!((ICookingPotHasChanged) this).sdtfc$getHasChanged()) return;
        if (!(this instanceof ICookingPotFluidAccess acc)) {return;}
        var tankHandler = acc.sd$getFluidHandler();
        FluidStack snapshot = FluidStack.EMPTY;
        if (tankHandler != null && tankHandler.getTanks() > 0) {
            snapshot = tankHandler.getFluidInTank(0).copy();
        }
        var wrapper =  new CookingPotFluidRecipeWrapper(this.inventory, snapshot);
        ItemStack dynamicResult = calcDynamicResult(wrapper, sdRecipe, level);
        ((ICookingPotHasChanged) this).sdtfc$setChanged(true);
        cir.setReturnValue(FoodCapability.areStacksStackableExceptCreationDate(dynamicResult, inventory.getStackInSlot(6)));
    }

    @ModifyExpressionValue(method = "processCooking", at = @At(value = "INVOKE", target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;assemble(Lnet/neoforged/neoforge/items/wrapper/RecipeWrapper;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack applyDynamic(ItemStack original, @Local(argsOnly = true) RecipeHolder<CookingPotRecipe> recipeHolder){
        if(!(recipeHolder.value() instanceof SDCookingPotRecipe sdRecipe)) return original;
        if(!sdRecipe.shouldCalcDynamic()) return original;
        if (!(this instanceof ICookingPotFluidAccess acc)) {return original;}
        var tankHandler = acc.sd$getFluidHandler();
        FluidStack snapshot = FluidStack.EMPTY;
        if (tankHandler != null && tankHandler.getTanks() > 0) {
            snapshot = tankHandler.getFluidInTank(0).copy();
        }
        var wrapper =  new CookingPotFluidRecipeWrapper(this.inventory, snapshot);
        return calcDynamicResult(wrapper, sdRecipe, level);
    }

    @Inject(method = "processCooking", at = @At("RETURN"))
    private void drainFluid(RecipeHolder<CookingPotRecipe> recipeHolder, CookingPotBlockEntity cookingPot, CallbackInfoReturnable<Boolean> cir){
        if(!cir.getReturnValue()) return;
        if(!(recipeHolder.value() instanceof SDCookingPotRecipe sdRecipe)) return;
        Objects.requireNonNull(((ICookingPotFluidAccess) cookingPot).sd$getFluidHandler()).drain(sdRecipe.getFluidAmountMb(), IFluidHandler.FluidAction.EXECUTE);
        ((ICookingPotHasChanged)this).sdtfc$setChanged(true);
    }


}

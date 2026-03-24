package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.bridge.ICookingPotTFCRecipeBridge;
import com.vomiter.survivorsdelight.common.device.cooking_pot.bridge.TFCPotRecipeBridgeFD;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.Optional;

@Mixin(CookingPotBlockEntity.class)
public abstract class CookingPotBlockEntity_TFCPotBridgeMixin extends SyncedBlockEntity implements ICookingPotTFCRecipeBridge {

    public CookingPotBlockEntity_TFCPotBridgeMixin(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    @Shadow protected abstract boolean hasInput();
    @Final @Shadow private ItemStackHandler inventory;

    @Unique private RecipeHolder<CookingPotRecipe> sdtfc$bridgeCached;

    @Override
    public RecipeHolder<CookingPotRecipe> sdtfc$getBridgeCached() {
        return sdtfc$bridgeCached;
    }

    @Override
    public void sdtfc$setBridgeCached(RecipeHolder<CookingPotRecipe> r) {
        sdtfc$bridgeCached = r;
    }

    @Inject(method = "getMatchingRecipe", at = @At("RETURN"), cancellable = true)
    private void sd$injectTFCBridge(RecipeWrapper wrapper, CallbackInfoReturnable<Optional<RecipeHolder<CookingPotRecipe>>> cir) {
        Optional<RecipeHolder<CookingPotRecipe>> original = cir.getReturnValue();
        if (original.isPresent()) {
            sdtfc$bridgeCached = null;
            return;
        }
        if (level == null || level.isClientSide || !hasInput()) {
            return;
        }

        if (sdtfc$bridgeCached != null) {
            CookingPotRecipe cached = sdtfc$bridgeCached.value();
            if (cached.matches(wrapper, level)) {
                cir.setReturnValue(Optional.of(sdtfc$bridgeCached));
                return;
            }
            sdtfc$bridgeCached = null;
        }

        IFluidHandler fluids = null;
        if ((Object) this instanceof ICookingPotFluidAccess access) {
            fluids = access.sd$getFluidHandler();
        }
        if (fluids == null) {
            return;
        }

        TFCPotRecipeBridgeFD bridge = TFCPotRecipeBridgeFD.bridge(level, inventory, fluids);
        if (bridge == null || bridge.getResultStack().isEmpty()) {
            return;
        }

        ResourceLocation id = SDUtils.RLUtils.build("tfc_bridge/" + getBlockPos().asLong());
        sdtfc$bridgeCached = new RecipeHolder<>(id, bridge);
        cir.setReturnValue(Optional.of(sdtfc$bridgeCached));
    }

    @Redirect(
            method = "canCook(Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z")
    )
    private boolean sdtfc$compareStacks(ItemStack a, ItemStack b) {
        if (FoodCapability.get(a) == null && FoodCapability.get(b) == null) {
            return ItemStack.isSameItem(a, b);
        }
        return FoodCapability.areStacksStackableExceptCreationDate(a, b);
    }

    @Redirect(
            method = "processCooking",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V")
    )
    private void sdtfc$mergeFoodStacks(ItemStack instance, int ignored, RecipeHolder<CookingPotRecipe> recipeHolder) {
        if (level == null) {
            instance.grow(ignored);
            return;
        }

        ItemStack resultStack = recipeHolder.value().getResultItem(level.registryAccess());
        if (FoodCapability.get(instance) == null && FoodCapability.get(resultStack) == null) {
            instance.grow(resultStack.getCount());
            return;
        }

        FoodCapability.mergeItemStacks(instance, resultStack.copy());
    }

    @Inject(method = "processCooking", at = @At("RETURN"))
    private void sdtfc$resetBridgeAfterCook(RecipeHolder<CookingPotRecipe> recipeHolder, CookingPotBlockEntity cookingPot, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            sdtfc$bridgeCached = null;
        }
    }
}

package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.cooking_pot.PotFluidPersistence;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import com.vomiter.survivorsdelight.registry.recipe.SDCookingPotRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

/*
This mixin handles how fluid works in cooking pot and how fluid requiring recipe is handled.
For TFC pot recipe bridge, please check LEGACY_CookingPotBlockEntity_PotRecipeBridgeMixin.java
 */
@Mixin(value = CookingPotBlockEntity.class, remap = false)
public abstract class CookingPotBlockEntity_FluidHandleMixin extends BlockEntity {

    public CookingPotBlockEntity_FluidHandleMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique
    private boolean sdtfc$warnedMissingFluidAccess = false;

    @Unique private static long sdtfc$lastServerTickWarnGameTime = -1;


    @Unique
    private void sdtfc$warnMissingFluidAccess(){
        if(!sdtfc$warnedMissingFluidAccess) {
            SurvivorsDelight.LOGGER.warn(
                    "[SD][CookingPot] Missing ICookingPotFluidAccess at {} (this={})",
                    getBlockPos(),
                    this.getClass().getName()
            );
            sdtfc$warnedMissingFluidAccess = true;
        }
    }

    @Unique
    private ICookingPotFluidAccess sdtfc$getFluidAccess(){
        var access = (Object) this instanceof ICookingPotFluidAccess a ? a : null;
        if(access == null) sdtfc$warnMissingFluidAccess();
        return access;
    }

    // ====== TFC barrel-like fluid input/output with fluid container items ======
    @Inject(method = "cookingTick", at = @At("HEAD"))
    private static void serverTick(Level level, BlockPos pos, BlockState state, CookingPotBlockEntity cookingPot, CallbackInfo ci) {
        if (level.isClientSide) return;
        if(cookingPot instanceof ICookingPotFluidAccess fluidAccess) {
            fluidAccess.sdtfc$updateFluidIOSlots();
        }
        else {
            long t = level.getGameTime();
            if (t - sdtfc$lastServerTickWarnGameTime > 200) { // 200 ticks = 10s
                SurvivorsDelight.LOGGER.warn(
                        "[SD][CookingPot] Cooking Pot at {} does not have proper interface: ICookingPotFluidAccess.",
                        pos
                );
                sdtfc$lastServerTickWarnGameTime = t;
            }
        }
    }

    // ====== Drain fluid ingredient upon finish ======
    @Inject(
            method = "processCooking",
            at = @At("RETURN"),
            remap = false
    )
    private void sdtfc$drainFluidWhenCooked(
            RecipeHolder<CookingPotRecipe> recipe,
            CookingPotBlockEntity cookingPot,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!cir.getReturnValue()) return; // 沒真的做出成品就不扣
        if (level == null) return;
        if(!(recipe.value() instanceof SDCookingPotRecipe sdCookingPotRecipe)) return;
        if(cookingPot instanceof ICookingPotFluidAccess access)
            access.sdtfc$getTank()
                    .drain(
                            sdCookingPotRecipe.getFluidAmountMb(),
                            IFluidHandler.FluidAction.EXECUTE
                    );
        else sdtfc$warnMissingFluidAccess();
    }

    // ====== 方塊破壞時清除 Caps =======
    @Inject(method = "setRemoved", at = @At("TAIL"), remap = true)
    private void sdtfc$setRemoved(CallbackInfo ci) {
    }

    // ====== NBT：載入 / 儲存 ======
    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = true)
    private void sdtfc$loadExtraData(CompoundTag compound, HolderLookup.Provider registries, CallbackInfo ci) {
        if(level == null) return;
        var access = sdtfc$getFluidAccess();
        if(access == null) return;
        PotFluidPersistence.load(level, compound, access);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = true)
    private void sdtfc$saveExtraData(CompoundTag compound, HolderLookup.Provider registries, CallbackInfo ci) {
        if(level == null) return;
        var access = sdtfc$getFluidAccess();
        if(access == null) return;
        PotFluidPersistence.save(level, compound, access);
    }

    // ====== 同步：update tag ======
    @Inject(method = "getUpdateTag", at = @At("RETURN"), cancellable = true, remap = true)
    private void sdtfc$appendExtraToUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
        if(level == null) return;
        CompoundTag out = cir.getReturnValue();
        var access = sdtfc$getFluidAccess();
        if(access == null) return;
        PotFluidPersistence.appendToUpdateTag(level, out, access);
        cir.setReturnValue(out);
    }

    @Override
    public void handleUpdateTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.handleUpdateTag(tag, registries);
        if(level == null) return;
        var access = sdtfc$getFluidAccess();
        if(access == null) return;
        PotFluidPersistence.handleUpdateTag(registries, tag, access);
    }
}

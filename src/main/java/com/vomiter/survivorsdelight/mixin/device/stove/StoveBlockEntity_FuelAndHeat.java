package com.vomiter.survivorsdelight.mixin.device.stove;

import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.compat.firmalife.StoveOvenCompat;
import com.vomiter.survivorsdelight.common.device.stove.IStoveBlockEntity;
//import com.vomiter.survivorsdelight.content.device.stove.StoveOvenCompat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;

@Mixin(value = StoveBlockEntity.class, remap = false)
public class StoveBlockEntity_FuelAndHeat implements HeatSourceBlockEntity, IStoveBlockEntity {
    @Unique private static final String SD_LEFT_BURN_TICK = "SDLeftBurnTick";
    @Unique private int leftBurnTick = 0;
    @Unique private final HeatingRecipe[] cachedHeatingRecipes = new HeatingRecipe[6];

    @Inject(method = "cookingTick", at = @At("HEAD"))
    private static void injectedCookingTick(Level level, BlockPos pos, BlockState state, StoveBlockEntity stove, CallbackInfo ci){
        var self = (IStoveBlockEntity)stove;
        if(self == null) return;
        if(state.getValue(StoveBlock.LIT) && self.sdtfc$getLeftBurnTick() > 0){
            if(level.getGameTime() % 20 == 0) self.sdtfc$reduceLeftBurnTick(1);
            if(ModList.get().isLoaded("firmalife")) StoveOvenCompat.ovenHeating(level, pos, state, stove);
            if(level.getGameTime() % 100 == 0){
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
        else if(state.getValue(StoveBlock.LIT) && state.getBlock() instanceof StoveBlock stoveBlock){
            stoveBlock.extinguish(state, level, pos);
        }
    }

    @Inject(method = "cookAndOutputItems", at = @At("HEAD"))
    private void cookTFCFood(CallbackInfo ci) {
        StoveBlockEntity stove = (StoveBlockEntity) (Object) this;
        IStoveBlockEntity iStove = (IStoveBlockEntity) stove;
        if(stove.getLevel() == null) return;
        int slots = stove.getInventory().getSlots();
        for(int i = 0; i < slots; i++){
            iStove.sdtfc$cookTFCFoodInSlot(stove, i);
        }
    }

    @Unique
    public int sdtfc$getLeftBurnTick(){return leftBurnTick;}

    @Unique
    public void sdtfc$setLeftBurnTick(int v){leftBurnTick = v;}

    @Override
    public HeatingRecipe[] sdtfc$getCachedRecipes() {
        return cachedHeatingRecipes;
    }

    @Override
    public float sdtfc$getTemperature() {
        if(!((BlockEntity)(Object)this).getBlockState().getValue(StoveBlock.LIT)) return 0;
        if(sdtfc$getLeftBurnTick() > 0){
            sdtfc$reduceLeftBurnTick(1);
            return 500;
        }
        return 0;
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = true)
    private void sdtfc$loadLeftBurnTick(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (tag.contains(SD_LEFT_BURN_TICK, 3)) { // 3 = int
            this.leftBurnTick = tag.getInt(SD_LEFT_BURN_TICK);
        }
    }

    @Inject(method = "writeItems", at = @At("TAIL"))
    private void sd$writeLeftBurnTick(CompoundTag tag, HolderLookup.Provider registries, CallbackInfoReturnable<CompoundTag> cir) {
        tag.putInt(SD_LEFT_BURN_TICK, this.leftBurnTick);
    }




}
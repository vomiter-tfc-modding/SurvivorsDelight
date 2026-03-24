package com.vomiter.survivorsdelight.common.device.stove;

import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.mixin.device.stove.StoveBlockEntity_Accessor;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTraits;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;
import vectorwing.farmersdelight.common.utility.ItemUtils;

public interface IStoveBlockEntity {
    int sdtfc$getLeftBurnTick();
    void sdtfc$setLeftBurnTick(int v);
    default void sdtfc$addLeftBurnTick(int v){
        sdtfc$setLeftBurnTick(sdtfc$getLeftBurnTick() + v);
    }
    default void sdtfc$reduceLeftBurnTick(int v){
        sdtfc$setLeftBurnTick(sdtfc$getLeftBurnTick() - v);
        if(sdtfc$getLeftBurnTick() < 0) sdtfc$setLeftBurnTick(0);
    }
    HeatingRecipe[] sdtfc$getCachedRecipes();

    static float sdtfc$getStaticTemperature(){
        return 550;
    }
    static int sdtfc$getMaxDuration(){ return 7 * 20 * 60 * 20;}

    default boolean sdtfc$addItem(ItemStack itemStackIn, int slot, StoveBlockEntity stove, Player player) {
        var inventory = stove.getInventory();
        if (0 <= slot && slot < inventory.getSlots()) {
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (slotStack.isEmpty()) {
                var recipe = HeatingRecipe.getRecipe((itemStackIn));
                if(recipe == null) return false;
                if(recipe.getTemperature() > 500) return false;
                assert stove.getLevel() != null;
                if(recipe.getResultItem(stove.getLevel().registryAccess()).isEmpty()) return false;
                var acc = (StoveBlockEntity_Accessor)stove;
                acc.getCookingTimesTotal()[slot] = 24 * 60 * 60 * 20;
                acc.getCookingTimes()[slot] = 0;
                inventory.setStackInSlot(slot,
                        player.isCreative()?
                                new ItemStack(itemStackIn.getItem()) :
                                itemStackIn.split(1)
                );
                stove.setChanged();
                Level level = stove.getLevel();
                if(level != null){
                    level.sendBlockUpdated(stove.getBlockPos(), stove.getBlockState(), stove.getBlockState(), 3);
                }
                return true;
            }
        }

        return false;
    }

    default void sdtfc$ejectItem(StoveBlockEntity stove, ItemStack item){
        Level level = stove.getLevel();
        BlockPos pos = stove.getBlockPos();
        if(level == null) return;
        ItemUtils.spawnItemEntity(
                level,
                item,
                (double)pos.getX() + (double)0.5F,
                (double)pos.getY() + (double)1.0F,
                (double)pos.getZ() + (double)0.5F,
                level.random.nextGaussian() * (double)0.01F, 0.1F,
                level.random.nextGaussian() * (double)0.01F);
    }

    default void sdtfc$cookTFCFoodInSlot(StoveBlockEntity stove, int slot){
        var inventory = stove.getInventory();
        Level level = stove.getLevel();
        BlockPos pos = stove.getBlockPos();
        if(level == null) return;
        ItemStack slotStack = inventory.getStackInSlot(slot);
        IHeat heat = HeatCapability.get(slotStack);
        if(heat != null){
            float heatingTemp = ((HeatSourceBlockEntity)stove).sdtfc$getTemperature();
            HeatCapability.addTemp(heat, heatingTemp);
            HeatingRecipe[] cachedRecipes = sdtfc$getCachedRecipes();
            if(cachedRecipes[slot] == null){
                var recipe = HeatingRecipe.getRecipe((slotStack));
                cachedRecipes[slot] = recipe;
            }
            if(cachedRecipes[slot] == null){
                int cookingTotalTime = ((StoveBlockEntity_Accessor)stove).getCookingTimesTotal()[slot];
                if(cookingTotalTime == 0){
                    sdtfc$ejectItem(stove, inventory.extractItem(slot, 1, false));
                    stove.setChanged();
                    level.sendBlockUpdated(pos, stove.getBlockState(), stove.getBlockState(), 3);
                }
            }
            else if(cachedRecipes[slot].isValidTemperature(heat.getTemperature())){
                assert stove.getLevel() != null;
                final ItemStack result = cachedRecipes[slot].assembleItem((slotStack));
                FoodCapability.applyTrait(result, FoodTraits.WOOD_GRILLED);
                sdtfc$ejectItem(stove, result.copy());
                inventory.extractItem(slot, 1, false);
                stove.setChanged();
                cachedRecipes[slot] = null;
                level.sendBlockUpdated(pos, stove.getBlockState(), stove.getBlockState(), 3);
            }
        }



    }


}

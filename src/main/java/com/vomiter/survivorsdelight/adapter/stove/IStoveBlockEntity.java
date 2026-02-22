package com.vomiter.survivorsdelight.adapter.stove;

import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.mixin.device.stove.StoveBlockEntity_Accessor;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.FoodTraits;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;
import vectorwing.farmersdelight.common.utility.ItemUtils;

public interface IStoveBlockEntity {
    int[] sdtfc$getCookingTimes();
    int[] sdtfc$getCookingTimesTotal();
    ResourceLocation[] sdtfc$getLastRecipeIDs();
    SyncedBlockEntity sdtfc$getBlockEntity();
    ItemStackHandler sdtfc$getInventory();


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

    default boolean sdtfc$addItem(ItemStack itemStackIn, int slot, IStoveBlockEntity stove, Player player) {
        var inventory = stove.sdtfc$getInventory();
        if (0 <= slot && slot < inventory.getSlots()) {
            ItemStack slotStack = inventory.getStackInSlot(slot);
            if (slotStack.isEmpty()) {
                var recipe = HeatingRecipe.getRecipe(new ItemStackInventory(itemStackIn));
                if(recipe == null) return false;
                if(recipe.getTemperature() > 500) return false;
                assert stove.sdtfc$getBlockEntity().getLevel() != null;
                if(recipe.getResultItem(stove.sdtfc$getBlockEntity().getLevel().registryAccess()).isEmpty()) return false;
                sdtfc$getCookingTimesTotal()[slot] = 24 * 60 * 60 * 20;
                sdtfc$getCookingTimes()[slot] = 0;
                inventory.setStackInSlot(slot,
                        player.isCreative()?
                                new ItemStack(itemStackIn.getItem()) :
                                itemStackIn.split(1)
                );
                sdtfc$getLastRecipeIDs()[slot] = recipe.getId();
                stove.sdtfc$getBlockEntity().setChanged();
                Level level = stove.sdtfc$getBlockEntity().getLevel();
                if(level != null){
                    level.sendBlockUpdated(stove.sdtfc$getBlockEntity().getBlockPos(), stove.sdtfc$getBlockEntity().getBlockState(), stove.sdtfc$getBlockEntity().getBlockState(), 3);
                }
                return true;
            }
        }

        return false;
    }

    default void sdtfc$ejectItem(IStoveBlockEntity stove, ItemStack item){
        Level level = stove.sdtfc$getBlockEntity().getLevel();
        BlockPos pos = stove.sdtfc$getBlockEntity().getBlockPos();
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

    default void sdtfc$cookTFCFoodInSlot(IStoveBlockEntity stove, int slot){
        var inventory = stove.sdtfc$getInventory();
        Level level = stove.sdtfc$getBlockEntity().getLevel();
        BlockPos pos = stove.sdtfc$getBlockEntity().getBlockPos();
        if(level == null) return;
        ItemStack slotStack = inventory.getStackInSlot(slot);
        IHeat heat = HeatCapability.get(slotStack);
        if(heat != null){
            float heatingTemp = ((HeatSourceBlockEntity)stove).sdtfc$getTemperature();
            HeatCapability.addTemp(heat, heatingTemp);
            HeatingRecipe[] cachedRecipes = sdtfc$getCachedRecipes();
            if(cachedRecipes[slot] == null){
                var recipe = HeatingRecipe.getRecipe(new ItemStackInventory(slotStack));
                cachedRecipes[slot] = recipe;
            }
            if(cachedRecipes[slot] == null){
                int cookingTotalTime = ((StoveBlockEntity_Accessor)stove).getCookingTimesTotal()[slot];
                if(cookingTotalTime == 0){
                    sdtfc$ejectItem(stove, inventory.extractItem(slot, 1, false));
                    stove.sdtfc$getBlockEntity().setChanged();
                    level.sendBlockUpdated(pos, stove.sdtfc$getBlockEntity().getBlockState(), stove.sdtfc$getBlockEntity().getBlockState(), 3);
                }
            }
            else if(cachedRecipes[slot].isValidTemperature(heat.getTemperature())){
                assert stove.sdtfc$getBlockEntity().getLevel() != null;
                final ItemStack result = cachedRecipes[slot].assemble(new ItemStackInventory(slotStack), stove.sdtfc$getBlockEntity().getLevel().registryAccess());
                FoodCapability.applyTrait(result, FoodTraits.WOOD_GRILLED);
                FoodCapability.updateFoodDecayOnCreate(result);
                sdtfc$ejectItem(stove, result.copy());
                inventory.extractItem(slot, 1, false);
                stove.sdtfc$getBlockEntity().setChanged();
                cachedRecipes[slot] = null;
                level.sendBlockUpdated(pos, stove.sdtfc$getBlockEntity().getBlockState(), stove.sdtfc$getBlockEntity().getBlockState(), 3);
            }
        }



    }


}

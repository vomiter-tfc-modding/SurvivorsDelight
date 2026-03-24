package com.vomiter.survivorsdelight.common.food.block;

import com.vomiter.survivorsdelight.compat.firmalife.FLCompatHelpers;
import com.vomiter.survivorsdelight.compat.firmalife.SDClimateReceiver;
import com.vomiter.survivorsdelight.compat.firmalife.SDClimateType;
import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

public abstract class SDDecayingBlockEntity extends DecayingBlockEntity  implements SDClimateReceiver {
    private boolean foodRotten = false;

    public SDDecayingBlockEntity(BlockEntityType type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SDDecayingBlockEntity blockEntity) {
        if (level.getGameTime() % 20L == 0L && blockEntity.isRotten() && !blockEntity.foodRotten) {
            blockEntity.foodRotten = true;
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Override public void saveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
    }

    @Override public void loadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
    }

    public void updatePreservation(boolean preserved) {
        if(!ModList.get().isLoaded("firmalife")) return;
        if (preserved)
        {
            FoodCapability.applyTrait(getStack(), FLCompatHelpers.getShelvedFoodTrait(this));
        }
        else
        {
            for (Holder<FoodTrait> trait : FLCompatHelpers.getPossibleShelvedFoodTraits())
            {
                FoodCapability.removeTrait(getStack(), trait);
            }
        }
    }

    @Override public void setValid(Level level, BlockPos pos, boolean valid, int tier, SDClimateType climate){
        boolean climateValid = climate.equals(SDClimateType.CELLAR) && valid;
        updatePreservation(climateValid);
    };


}

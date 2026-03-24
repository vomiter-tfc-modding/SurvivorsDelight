package com.vomiter.survivorsdelight.adapter.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.ICookingPotFluidAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class PotFluidPersistence {
    private PotFluidPersistence() {}

    public static final String KEY_TANK = "survivorsdelight:pot_tank";
    public static final String KEY_AUX  = "survivorsdelight:aux_inv";

    public static void load(Level level, CompoundTag compound, ICookingPotFluidAccess access) {
        if (compound.contains(KEY_TANK, Tag.TAG_COMPOUND)) {
            var fluidHandler = access.sdtfc$getTank();
            if(fluidHandler instanceof FluidTank fluidTank){
                fluidTank.readFromNBT(level.registryAccess(), compound.getCompound(KEY_TANK));
            }
        }
        if (compound.contains(KEY_AUX, Tag.TAG_COMPOUND)) {
            access.sdtfc$getAuxInv().deserializeNBT(level.registryAccess(), compound.getCompound(KEY_AUX));
        }
    }

    public static void save(Level level, CompoundTag compound, ICookingPotFluidAccess access) {
        CompoundTag tank = new CompoundTag();
        var fluidHandler = access.sdtfc$getTank();
        if(fluidHandler instanceof FluidTank fluidTank){
            fluidTank.writeToNBT(level.registryAccess(), tank);
        }
        compound.put(KEY_TANK, tank);
        CompoundTag aux = access.sdtfc$getAuxInv().serializeNBT(level.registryAccess());
        compound.put(KEY_AUX, aux);
    }

    public static void appendToUpdateTag(Level level, CompoundTag out, ICookingPotFluidAccess access) {
        save(level, out, access);
    }

    public static void handleUpdateTag(HolderLookup.Provider registries, CompoundTag tag, ICookingPotFluidAccess access) {
        if (tag.contains(KEY_TANK, Tag.TAG_COMPOUND)) {
            var fluidHandler = access.sdtfc$getTank();
            if(fluidHandler instanceof FluidTank fluidTank){
                fluidTank.readFromNBT(registries, tag.getCompound(KEY_TANK));
            }
        }
        if (tag.contains(KEY_AUX, Tag.TAG_COMPOUND)) {
            access.sdtfc$getAuxInv().deserializeNBT(registries, tag.getCompound(KEY_AUX));
        }
    }
}

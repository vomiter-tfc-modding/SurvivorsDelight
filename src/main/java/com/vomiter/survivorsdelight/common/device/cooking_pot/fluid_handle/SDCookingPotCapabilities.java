package com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;

public class SDCookingPotCapabilities {
    public static final BlockCapability<IItemHandler, Direction> AUX_INV =
            BlockCapability.createSided(SDUtils.RLUtils.build(SurvivorsDelight.MODID, ("aux_inv")), IItemHandler.class);
    public static void onRegisterCaps(RegisterCapabilitiesEvent event) {

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntityTypes.COOKING_POT.get(),
                (be, side) -> (be instanceof ICookingPotFluidAccess acc) ? acc.sd$getFluidHandler() : null
        );

        event.registerBlockEntity(
                AUX_INV,
                ModBlockEntityTypes.COOKING_POT.get(),
                (be, side) -> {
                    if (!(be instanceof ICookingPotFluidAccess acc)) return null;
                    if (side == Direction.UP || side == Direction.DOWN) return acc.sdtfc$getAuxInv();
                    return null;
                }
        );
    }
}

package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.common.container.SDCabinetMenu;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotFluidMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;

public class SDContainerTypes {
    public static final DeferredRegister<MenuType<?>> CONTAINERS =
            DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SDCabinetMenu>> CABINET =
            CONTAINERS.register("cabinet", () -> IMenuTypeExtension.create(SDCabinetMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<SDCookingPotFluidMenu>> POT_FLUID_MENU =
            CONTAINERS.register("pot_fluid", () -> IMenuTypeExtension.create(SDCookingPotFluidMenu::new));
}

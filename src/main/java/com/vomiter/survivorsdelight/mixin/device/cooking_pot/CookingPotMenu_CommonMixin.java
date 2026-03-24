package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotCommonMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

@Mixin(CookingPotMenu.class)
public class CookingPotMenu_CommonMixin implements ICookingPotCommonMenu {
    @Shadow @Final public CookingPotBlockEntity blockEntity;

    @Override
    public CookingPotBlockEntity sdtfc$getBlockEntity() {
        return blockEntity;
    }
}

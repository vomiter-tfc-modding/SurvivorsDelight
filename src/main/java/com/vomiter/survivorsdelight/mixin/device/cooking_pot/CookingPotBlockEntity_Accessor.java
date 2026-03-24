package com.vomiter.survivorsdelight.mixin.device.cooking_pot;

import net.minecraft.world.inventory.ContainerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(CookingPotBlockEntity.class)
public interface CookingPotBlockEntity_Accessor {
    @Accessor("cookingPotData")
    ContainerData getCookingPotData();
}

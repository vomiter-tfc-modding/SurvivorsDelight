package com.vomiter.survivorsdelight.mixin;

import net.dries007.tfc.common.blockentities.InventoryBlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = InventoryBlockEntity.class, remap = false)
public interface InventoryBlockEntityAccessor {
}

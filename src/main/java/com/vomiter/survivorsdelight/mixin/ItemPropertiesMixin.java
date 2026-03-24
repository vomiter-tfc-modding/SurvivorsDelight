package com.vomiter.survivorsdelight.mixin;

import com.vomiter.survivorsdelight.registry.ItemPropertyInterface;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin implements ItemPropertyInterface {
    @Shadow
    abstract DataComponentMap buildAndValidateComponents();

    @Override
    public DataComponentMap sdtfc$getMap() {
        return buildAndValidateComponents();
    }
}

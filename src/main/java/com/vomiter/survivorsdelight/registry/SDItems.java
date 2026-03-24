package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsabilities.core.SAEffects;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class SDItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, SurvivorsDelight.MODID);

    public static final DeferredHolder<Item, Item> EFFECT_NOURISHMENT =
            ITEMS.register("effect_icon/nourishment", () -> new Item(new Item.Properties()){
                @Override public @NotNull String getDescriptionId() {
                    return ModEffects.NOURISHMENT.value().getDescriptionId();
                }
            });

    public static final DeferredHolder<Item, Item> EFFECT_COMFORT =
            ITEMS.register("effect_icon/comfort", () -> new Item(new Item.Properties()){
                @Override public @NotNull String getDescriptionId() {
                    return ModEffects.COMFORT.value().getDescriptionId();
                }
            });

    public static final DeferredHolder<Item, Item> EFFECT_WORKHORSE =
            ITEMS.register("effect_icon/workhorse", () -> new Item(new Item.Properties()){
                @Override public @NotNull String getDescriptionId() {
                    return SAEffects.WORKHORSE.value().getDescriptionId();
                }
            });
}

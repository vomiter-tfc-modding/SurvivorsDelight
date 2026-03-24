package com.vomiter.survivorsdelight.registry;

import com.mojang.serialization.MapCodec;
import com.vomiter.survivorsdelight.registry.recipe.outputs.ReturnFoodContainerModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifierType;
import net.dries007.tfc.common.recipes.outputs.ItemStackModifiers;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;

/**
 * 登錄自訂的 ItemStackModifierType。
 */
public final class SDItemStackModifiers {
    private SDItemStackModifiers() {}

    // 這裡用 TFC 自己的登錄 key：ItemStackModifiers.KEY
    public static final DeferredRegister<ItemStackModifierType<?>> TYPES =
            DeferredRegister.create(ItemStackModifiers.KEY, MODID);

    // 對於「無參數」的 modifier，Codec 與 StreamCodec 都可以用 unit/constant
    public static final Id<ReturnFoodContainerModifier> RETURN_FOOD_CONTAINER =
            register("return_food_container", ReturnFoodContainerModifier.INSTANCE);

    private static <T extends ItemStackModifier> Id<T> register(String name, T instance) {
        return new Id<>(TYPES.register(name,
                () -> new ItemStackModifierType<>(MapCodec.unit(instance), StreamCodec.unit(instance))));
    }

    public record Id<T extends ItemStackModifier>(
            DeferredHolder<ItemStackModifierType<?>, ItemStackModifierType<T>> holder
    ) {}

}

package com.vomiter.survivorsdelight.common.food.block;

import com.vomiter.survivorsdelight.compat.firmalife.FLCompatHelpers;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodData;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.IFood;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class DecayFoodTransfer {
    private DecayFoodTransfer() {}

    /**
     * Copy TFC food state from src to dst.
     * - creation date
     * - traits (replace)
     * - dynamic food data (replace)
     * - optional Firmalife trait stripping
     * Returns dst for chaining.
     */
    public static ItemStack copyFoodState(ItemStack src, ItemStack dst, boolean stripFirmalifeShelvedTraits, float factor) {
        IFood srcFood = FoodCapability.get(src);
        IFood dstFood = FoodCapability.get(dst);
        if (srcFood == null || dstFood == null) return dst;

        // creation date
        FoodCapability.setCreationDate(dst, srcFood.getCreationDate());

        // traits: replace (avoid duplication)
        dstFood.getTraits().clear();
        dstFood.getTraits().addAll(srcFood.getTraits());

        // remove Firmalife-specific traits if requested
        if (stripFirmalifeShelvedTraits && ModList.get().isLoaded("firmalife")) {
            for (Holder<FoodTrait> t : FLCompatHelpers.getPossibleShelvedFoodTraits()) {
                FoodCapability.removeTrait(dst, t);
            }
        }

        // dynamic food data: replace
        FoodCapability.setFoodForDynamicItemOnCreate(dst, scaleNutrients(srcFood.getData(), factor));
        return dst;
    }

    public static ItemStack copyFoodState(ItemStack src, ItemStack dst, boolean stripFirmalifeShelvedTraits) {
        return copyFoodState(src, dst, stripFirmalifeShelvedTraits, 1);
    }


    private static FoodData scaleNutrients(FoodData src, float factor) {
        factor = Math.max(0.0f, factor);

        float[] nutrients = src.nutrients(); // 這裡會 clone，一般是安全的
        for (int i = 0; i < nutrients.length; i++) {
            nutrients[i] *= factor;
        }

        return new FoodData(
                src.hunger(),
                src.water(),
                src.saturation(),
                src.intoxication(),
                nutrients,
                src.decayModifier()
        );
    }
}

package com.vomiter.survivorsdelight.compat.firmalife;

import com.eerussianguy.firmalife.common.items.FLFoodTraits;
import com.eerussianguy.firmalife.config.FLConfig;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FLCompatHelpers {
    public static Holder<FoodTrait> getShelvedFoodTrait(BlockEntity be)
    {
        if (be.getLevel() != null)
        {
            final float temp = Climate.getAverageTemperature(be.getLevel(), be.getBlockPos());
            if (temp < FLConfig.SERVER.cellarLevel3Temperature.get())
            {
                return FLFoodTraits.SHELVED_3;
            }
            if (temp < FLConfig.SERVER.cellarLevel2Temperature.get())
            {
                return FLFoodTraits.SHELVED_2;
            }
        }
        return FLFoodTraits.SHELVED;
    }

    public static Holder<FoodTrait>[] getPossibleShelvedFoodTraits(){
        return new Holder[]{FLFoodTraits.SHELVED, FLFoodTraits.SHELVED_2, FLFoodTraits.SHELVED_3};
    }

}

package com.vomiter.survivorsdelight.common.device.skillet;

import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.heat.HeatCapability;
import net.dries007.tfc.common.component.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

public class SkilletUtil {
    public static final DeferredHolder<FoodTrait, FoodTrait> skilletCooked = SDFoodTraits.SKILLET_COOKED;

    public static int extraHurtForTemperature(ItemStack skilletStack, float temperature){
        IHeat skilletHeat = HeatCapability.get(skilletStack);
        if(skilletHeat == null) return 0;
        HeatingRecipe heatingRecipe = HeatingRecipe.getRecipe((skilletStack));
        if(heatingRecipe != null){
            if(heatingRecipe.isValidTemperature(temperature)){
                return 3;
            }
        }
        if(temperature >= skilletHeat.getWeldingTemperature()) return 2;
        else if(temperature >= skilletHeat.getWorkingTemperature()) return 1;
        return 0;
    }
}
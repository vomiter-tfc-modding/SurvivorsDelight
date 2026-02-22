package com.vomiter.survivorsdelight.adapter.skillet;

import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import net.dries007.tfc.common.capabilities.food.FoodTrait;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.minecraft.world.item.ItemStack;

public class SkilletUtil {
    public static final FoodTrait skilletCooked = SDFoodTraits.SKILLET_COOKED;

    public static int extraHurtForTemperature(ItemStack skilletStack, float temperature){
        IHeat skilletHeat = HeatCapability.get(skilletStack);
        if(skilletHeat == null) return 0;
        HeatingRecipe heatingRecipe = HeatingRecipe.getRecipe(new ItemStackInventory(skilletStack));
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

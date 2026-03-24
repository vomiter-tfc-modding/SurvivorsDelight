package com.vomiter.survivorsdelight.common.device.stove;

/*
========
package com.vomiter.survivorsdelight.compat.firmalife;

>>>>>>>> master:src/main/java/com/vomiter/survivorsdelight/compat/firmalife/StoveOvenCompat.java
import com.eerussianguy.firmalife.common.blockentities.ApplianceBlockEntity;
import com.vomiter.survivorsdelight.HeatSourceBlockEntity;
import com.vomiter.survivorsdelight.adapter.stove.IStoveBlockEntity;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StoveOvenCompat {
    public static void ovenHeating(Level level, BlockPos pos, BlockState state, IStoveBlockEntity stove){
        assert stove != null;
        var above = level.getBlockEntity(pos.above());
        if(above instanceof ApplianceBlockEntity ovenTop){
            ovenTop.getCapability(Capabilities.ITEM).ifPresent(inventory -> {
                int heatLvl = 0;
                float temperature = 0;
                for(int i = 0; i < inventory.getSlots(); i++){
                    if(!inventory.getStackInSlot(i).isEmpty()) heatLvl ++;
                    temperature = ((HeatSourceBlockEntity)stove).sdtfc$getTemperature();
                }
                if(heatLvl != 0){
                    float finalTemperature = temperature;
                    above.getCapability(HeatCapability.BLOCK_CAPABILITY).ifPresent(cap -> cap.setTemperatureIfWarmer(finalTemperature));
                }
            });

        }
    }

    public static void interactionRegister(){

    }
}


 */
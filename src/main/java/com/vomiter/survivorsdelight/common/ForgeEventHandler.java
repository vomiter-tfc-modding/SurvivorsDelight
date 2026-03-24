package com.vomiter.survivorsdelight.common;

import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.common.device.skillet.itemcooking.SkilletCookingCap;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import vectorwing.farmersdelight.common.block.StoveBlock;

public class ForgeEventHandler {
    private static boolean registered = false;

    public static void init(){
        if (registered) return;
        registered = true;

        final IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(ForgeEventHandler::onFireStart);
        bus.addListener(SkilletCookingCap::onClone);
        bus.addListener(SDSkilletItem.SDSkilletEvents::playSkilletAttackSound);
    }

    public static void onFireStart(StartFireEvent event){
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = event.getState();
        Block block = state.getBlock();
        if(block instanceof StoveBlock){
            if(!state.getValue(StoveBlock.LIT)){
                level.setBlockAndUpdate(pos, state.setValue(StoveBlock.LIT, true));
                event.setCanceled(true);
            }
        }

    }

}

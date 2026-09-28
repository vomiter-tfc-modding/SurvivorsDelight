package com.vomiter.survivorsdelight.common;

import com.vomiter.survivorsdelight.common.command.SDFoodFallbackCommand;
import com.vomiter.survivorsdelight.common.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.data.food.SDFoodFallBackManager;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import vectorwing.farmersdelight.common.block.StoveBlock;

public class ForgeEventHandler {
    private static boolean registered = false;

    public static void init(){
        if (registered) return;
        registered = true;

        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(ForgeEventHandler::onFireStart);
        bus.addListener(SDSkilletItem.SDSkilletEvents::playSkilletAttackSound);
        bus.addListener(SDSkilletItem.SDSkilletEvents::onPlayerTick);

        bus.addListener(RichSoilDelayedCheck::onPlayerRightClick_RichSoilFarmGating);
        bus.addListener(RichSoilDelayedCheck::onServerTick);
        bus.addListener(SDFoodFallBackManager::onAddReloadListener);
        bus.addListener(ForgeEventHandler::onRegisterCommands);

    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SDFoodFallbackCommand.register(event.getDispatcher());
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

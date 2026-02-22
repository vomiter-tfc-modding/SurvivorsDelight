package com.vomiter.survivorsdelight.common;

import com.vomiter.survivorsdelight.SDConfig;
import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.adapter.skillet.skillet_item.SkilletCookingCap;
import net.dries007.tfc.common.blockentities.CropBlockEntity;
import net.dries007.tfc.util.events.StartFireEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import vectorwing.farmersdelight.common.block.StoveBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

public class ForgeEventHandler {
    private static boolean registered = false;

    public static void init(){
        if (registered) return;
        registered = true;

        final IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(ForgeEventHandler::onFireStart);

        bus.addListener(SkilletCookingCap::onClone);
        bus.addGenericListener(Entity.class, SkilletCookingCap::onAttachCapabilities);

        bus.addListener(SDSkilletItem.SDSkilletEvents::playSkilletAttackSound);

        bus.addListener(RichSoilDelayedCheck::onPlayerRightClick_RichSoilFarmGating);
        bus.addListener(RichSoilDelayedCheck::onServerTick);

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

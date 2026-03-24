package com.vomiter.survivorsdelight.common.farming;

import com.vomiter.survivorsdelight.mixin.BlockEntityTypeAccessor;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import vectorwing.farmersdelight.common.registry.ModBlocks;

import java.util.HashSet;

public class RichSoilFarmlandBlockEntitySetup {
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityType<FarmlandBlockEntity> type = TFCBlockEntities.FARMLAND.get();
            BlockEntityTypeAccessor acc = (BlockEntityTypeAccessor)type;
            HashSet<Block> validBlocks = new HashSet<>(acc.getValidBlocks());
            acc.setValidBlocks(validBlocks);
            validBlocks.add(ModBlocks.RICH_SOIL_FARMLAND.get());
        });
    }
}

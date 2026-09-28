package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.cabinet.SDCabinetBlockEntity;
import com.vomiter.survivorsdelight.common.food.block.DecayingFeastBlockEntity;
import com.vomiter.survivorsdelight.common.food.block.DecayingPieBlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.PieBlock;
import vectorwing.farmersdelight.common.registry.ModBlocks;

public class SDBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SurvivorsDelight.MODID);
    private static Block[] cabinetBlocks() {
        return SDBlocks.CABINETS.values()
                .stream()
                .map(RegistryObject::get)
                .toArray(Block[]::new);
    }
    public static final RegistryObject<BlockEntityType<SDCabinetBlockEntity>> SD_CABINET =
            BLOCK_ENTITIES.register(
                    "cabinet",
                    () -> BlockEntityType.Builder.of(
                            SDCabinetBlockEntity::new,
                            cabinetBlocks()
                    ).build(null));

    static Block[] feasts = ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof FeastBlock).toArray(Block[]::new);
    public static final RegistryObject<BlockEntityType<DecayingFeastBlockEntity>> FEAST_DECAYING =
            BLOCK_ENTITIES.register("feast_decaying",
                    () -> BlockEntityType.Builder.of(
                            DecayingFeastBlockEntity::new,
                            feasts
                    ).build(null));

    static Block[] pies = ForgeRegistries.BLOCKS.getValues().stream().filter(block -> block instanceof PieBlock).toArray(Block[]::new);
    public static final RegistryObject<BlockEntityType<DecayingPieBlockEntity>> PIE_DECAYING =
            BLOCK_ENTITIES.register("pie_decaying",
                    () -> BlockEntityType.Builder.of(
                            DecayingPieBlockEntity::new,
                            pies
                    ).build(null));


}

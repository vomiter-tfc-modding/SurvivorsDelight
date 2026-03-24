package com.vomiter.survivorsdelight.registry.skillet;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.mixin.BlockEntityTypeAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.block.SkilletBlock;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Supplier;

public class SDSkilletBlocks {
    private SDSkilletBlocks() {}
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.createBlocks(SurvivorsDelight.MODID);

    public static final Map<SkilletMaterial, Supplier<Block>> SKILLETS = new EnumMap<>(SkilletMaterial.class);
    public static Supplier<Block> get(SkilletMaterial m){
        return SKILLETS.get(m);
    }
    public static ResourceKey<Block> getKey(SkilletMaterial m){
        ResourceLocation rl = BuiltInRegistries.BLOCK.getKey(SKILLETS.get(m).get());
        return ResourceKey.create(Registries.BLOCK, rl);
    }

    public static final Supplier<Block> FARMER = BLOCKS.register("skillet/farmer", () ->
            new SkilletBlock(
                    BlockBehaviour.Properties
                            .of()
                            .mapColor(MapColor.METAL)
                            .noOcclusion()
                            .strength(0.5f, 6.0f)
                            .sound(SoundType.LANTERN)
            ));

    static {
        for (SkilletMaterial m : SkilletMaterial.values()) {
            Supplier<Block> ro = BLOCKS.register(m.path(), () ->
                    new SkilletBlock(BlockBehaviour.Properties
                            .of()
                            .mapColor(MapColor.METAL)
                            .noOcclusion()
                            .strength(0.5f, 6.0f)
                            .sound(SoundType.LANTERN)
                    )
            );
            SKILLETS.put(m, ro);
        }
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            BlockEntityType<SkilletBlockEntity> type = ModBlockEntityTypes.SKILLET.get();
            BlockEntityTypeAccessor acc = (BlockEntityTypeAccessor)type;
            HashSet<Block> validBlocks = new HashSet<>(acc.getValidBlocks());
            acc.setValidBlocks(validBlocks);
            SKILLETS.values().forEach(b -> validBlocks.add(b.get()));
            validBlocks.add(FARMER.get());
        });
    }

}
package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.container.SDCabinetBlock;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public class SDBlocks {

    // 1) 以 Vanilla Registries 建立 DeferredRegister（1.21 推薦）
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, SurvivorsDelight.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(Registries.ITEM, SurvivorsDelight.MODID);

    // 2) 以 Wood 做對應表：使用 DeferredHolder（取代舊 RegistryObject）
    public static final Map<Wood, DeferredHolder<Block, Block>> CABINETS = new EnumMap<>(Wood.class);

    static {
        for (Wood wood : Wood.values()) {
            final String name = "planks/cabinet/" + wood.getSerializedName();

            // 先註冊方塊
            final DeferredHolder<Block, Block> blockHolder = BLOCKS.register(
                    name,
                    () -> new SDCabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL))
            );

            // 再註冊對應的 BlockItem（用 holder.get()）
            BLOCK_ITEMS.register(
                    name,
                    () -> new BlockItem(blockHolder.get(), new Item.Properties())
            );

            CABINETS.put(wood, blockHolder);
        }
    }

    // 3) 對外提供註冊到 bus 的方法
    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ITEMS.register(modEventBus);
    }
}

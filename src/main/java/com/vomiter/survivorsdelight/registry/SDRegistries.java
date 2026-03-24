package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import net.neoforged.bus.api.IEventBus;

public class SDRegistries {
    public static void register(IEventBus modBus){
        SDSkilletBlocks.BLOCKS.register(modBus);
        SDSkilletItems.ITEMS.register(modBus);
        SDSkilletPartItems.ITEMS.register(modBus);
        SDBlocks.BLOCKS.register(modBus);
        SDBlocks.BLOCK_ITEMS.register(modBus);
        SDItems.ITEMS.register(modBus);
        SDBlockEntityTypes.BLOCK_ENTITIES.register(modBus);
        SDContainerTypes.CONTAINERS.register(modBus);
        SDCreativeTab.TABS.register(modBus);
        SDRecipeSerializers.SERIALIZERS.register(modBus);
        SDItemStackModifiers.TYPES.register(modBus);
        SDFoodTraits.TRAITS.register(modBus);
        SDDataComponents.DATA_COMPONENT_TYPES.register(modBus);
    }
}

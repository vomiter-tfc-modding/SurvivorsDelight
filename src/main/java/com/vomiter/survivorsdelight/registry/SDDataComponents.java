package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.registry.component.SDContainer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SDDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SurvivorsDelight.MODID);

    public static final DeferredHolder<DataComponentType<?> ,DataComponentType<SDContainer>> FOOD_CONTAINER =
            DATA_COMPONENT_TYPES.register("food_container", () ->
                    DataComponentType.<SDContainer>builder()
                            .persistent(SDContainer.CODEC)              // JSON/存檔
                            .networkSynchronized(SDContainer.STREAM_CODEC) // 封包同步
                            .cacheEncoding()
                            .build()
            );

}

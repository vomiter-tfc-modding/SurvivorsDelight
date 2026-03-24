package com.vomiter.survivorsdelight;

import com.mojang.logging.LogUtils;
import com.vomiter.survivorsdelight.client.ClientForgeEventHandler;
import com.vomiter.survivorsdelight.client.SaladPredicates;
import com.vomiter.survivorsdelight.client.SandwichPredicates;
import com.vomiter.survivorsdelight.common.ForgeEventHandler;
import com.vomiter.survivorsdelight.common.container.SDCabinetBlockEntity;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotCapabilities;
import com.vomiter.survivorsdelight.common.device.skillet.itemcooking.SkilletCookingCap;
import com.vomiter.survivorsdelight.common.farming.RichSoilFarmlandBlockEntitySetup;
import com.vomiter.survivorsdelight.registry.SDRegistries;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import com.vomiter.survivorsdelight.data.food.SDFoodAndRecipeGenerator;
import com.vomiter.survivorsdelight.network.SDNetwork;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SurvivorsDelight.MODID)
public class SurvivorsDelight {
    //TODO: add aquaculture support
    //TODO: add tfc cs compat
    //TODO: FL oils as food cooking oil

    //TODO: test effect in actual instance

    //TODO: another mod - Basket and storage blocks
    //TODO: another mod - Unroasted block and buildable feast
    //TODO: another mod - Beneath edition

    public static final String MODID = "survivorsdelight";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SDFoodAndRecipeGenerator foodAndCookingGenerator = new SDFoodAndRecipeGenerator(MODID);

    public SurvivorsDelight(ModContainer mod, IEventBus modBus) {
        init(modBus);
        mod.registerConfig(
                ModConfig.Type.COMMON,
                SDConfig.COMMON_SPEC
        );
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
        });
    }

    private void commonSetup(IEventBus modBus) {
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(RichSoilFarmlandBlockEntitySetup::onCommonSetup);
        modBus.addListener(SDSkilletBlocks::onCommonSetup);
        modBus.addListener(SkilletCookingCap::onRegisterCaps);
        modBus.addListener(SDCookingPotCapabilities::onRegisterCaps);
        modBus.addListener(SDCabinetBlockEntity::onRegisterCapabilities);

    }

    private void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            SandwichPredicates.addPredicate();
            SaladPredicates.addPredicate();
        });
    }

    private void init(IEventBus modBus) {
        SDRegistries.register(modBus);
        commonSetup(modBus);
        modBus.addListener(SDNetwork::onRegisterPayloads);


        // 你自己的 Forge/NeoForge 事件掛載（名稱不改也可）
        ForgeEventHandler.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientForgeEventHandler.init();
            modBus.addListener(ClientForgeEventHandler::registerMenuScreens);
            modBus.addListener(this::onClientSetup);
            modBus.addListener(ClientForgeEventHandler::registerClientExtensions);
            modBus.addListener(ClientForgeEventHandler::onClientSetup);

        }
    }
}

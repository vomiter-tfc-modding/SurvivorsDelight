package com.vomiter.survivorsdelight;

import com.mojang.logging.LogUtils;
import com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic.CookingPotDynamicRules;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid_handle.SDCookingPotCapabilities;
import com.vomiter.survivorsdelight.client.ClientForgeEventHandler;
import com.vomiter.survivorsdelight.common.ForgeEventHandler;
import com.vomiter.survivorsdelight.common.container.SDCabinetBlockEntity;
import com.vomiter.survivorsdelight.common.farming.RichSoilFarmlandBlockEntitySetup;
import com.vomiter.survivorsdelight.common.food.FoodContainerExpansion;
import com.vomiter.survivorsdelight.data.food.SDFoodAndRecipeGenerator;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.registry.SDRegistries;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import vectorwing.farmersdelight.common.tag.CommonTags;

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
    public static ResourceLocation modLoc(String path){ return ResourceLocation.fromNamespaceAndPath(MODID, path);}

    public SurvivorsDelight(ModContainer mod, IEventBus modBus) {
        init(modBus);
        mod.registerConfig(
                ModConfig.Type.COMMON,
                SDConfig.COMMON_SPEC
        );
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FoodContainerExpansion.register(Items.BOWL, stack -> stack.is(TFCBlocks.CERAMIC_BOWL.asItem()));
            FoodContainerExpansion.register(Items.GLASS_BOTTLE, stack -> stack.is(TFCTags.Items.GLASS_BOTTLES));
        });
    }

    private void commonSetup(IEventBus modBus) {
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(RichSoilFarmlandBlockEntitySetup::onCommonSetup);
        modBus.addListener(SDSkilletBlocks::onCommonSetup);
        modBus.addListener(SDCookingPotCapabilities::onRegisterCaps);
        modBus.addListener(SDCabinetBlockEntity::onRegisterCapabilities);
        modBus.addListener(CookingPotDynamicRules::onCommonSetup);
        modBus.addListener(CookingPotDynamicRules::onLoadComplete);

    }

    private void init(IEventBus modBus) {
        SDRegistries.register(modBus);
        commonSetup(modBus);
        modBus.addListener(SDNetwork::onRegisterPayloads);

        ForgeEventHandler.init();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientForgeEventHandler.init();
            modBus.addListener(ClientForgeEventHandler::registerMenuScreens);
            modBus.addListener(ClientForgeEventHandler::registerClientExtensions);
            modBus.addListener(ClientForgeEventHandler::onClientSetup);
        }
    }
}

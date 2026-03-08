package com.vomiter.survivorsdelight;

import com.mojang.logging.LogUtils;
import com.vomiter.survivorsdelight.adapter.cooking_pot.CookingPotExtraNutrientRules;
import com.vomiter.survivorsdelight.client.ClientForgeEventHandler;
import com.vomiter.survivorsdelight.client.SaladPredicates;
import com.vomiter.survivorsdelight.client.SandwichPredicates;
import com.vomiter.survivorsdelight.client.screen.SDCabinetScreen;
import com.vomiter.survivorsdelight.client.screen.SDPotFluidScreen;
import com.vomiter.survivorsdelight.common.ForgeEventHandler;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.SDCookingPotFluidMenu;
import com.vomiter.survivorsdelight.client.SkilletModels;
import com.vomiter.survivorsdelight.adapter.skillet.skillet_item.ISkilletItemCookingData;
import com.vomiter.survivorsdelight.adapter.farming.RichSoilFarmlandBlockEntitySetup;
import com.vomiter.survivorsdelight.common.food.FoodContainerExpansion;
import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import com.vomiter.survivorsdelight.data.food.SDFoodAndRecipeGenerator;
import com.vomiter.survivorsdelight.network.SDNetwork;
import com.vomiter.survivorsdelight.registry.SDContainerTypes;
import com.vomiter.survivorsdelight.registry.SDItemStackModifiers;
import com.vomiter.survivorsdelight.registry.SDRegistries;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(SurvivorsDelight.MODID)
public class SurvivorsDelight {
    //TODO: add aquaculture support
    //TODO: add tfc cs compat
    //TODO: add firmalife cellar compat for feast and pie

    //TODO: add familiarity config for ham looting
    //TODO: transfer manual recipes to datagen

    //TODO: another mod - Basket and storage blocks
    //TODO: another mod - Unroasted block and buildable feast
    //TODO: another mod - Beneath edition

    public static final String MODID = "survivorsdelight";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SDFoodAndRecipeGenerator foodAndCookingGenerator = new SDFoodAndRecipeGenerator(MODID);

    public SurvivorsDelight(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        init(modBus);
    }

    public SurvivorsDelight() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        init(modBus);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event){
        event.enqueueWork(() -> {
            FoodContainerExpansion.register(
                    Items.BOWL,
                    (stack -> stack.is(TFCBlocks.CERAMIC_BOWL.get().asItem()))
            );
            FoodContainerExpansion.register(
                    Items.GLASS_BOTTLE,
                    (stack -> stack.is(TFCItems.SILICA_GLASS_BOTTLE.get()))
            );
            CookingPotExtraNutrientRules.bootstrap();
        });
    }

    private void commonSetup(IEventBus modBus) {
        modBus.addListener(this::onCommonSetup);
        modBus.addListener(SDNetwork::onCommonSetup);
        modBus.addListener(RichSoilFarmlandBlockEntitySetup::onCommonSetup);
        modBus.addListener(SDSkilletBlocks.Compat::onCommonSetup);
        modBus.addListener(SDItemStackModifiers::onCommonSetUp);
    }

    public void onClientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(SDContainerTypes.CABINET.get(), SDCabinetScreen::new);
            MenuScreens.register(SDCookingPotFluidMenu.TYPE, SDPotFluidScreen::new);
            SandwichPredicates.addPredicate();
            SaladPredicates.addPredicate();
        });
    }

    public void init(IEventBus modBus){

        SDFoodTraits.bootstrap();

        SDRegistries.register(modBus);
        commonSetup(modBus);
        modBus.addListener((RegisterCapabilitiesEvent e) -> e.register(ISkilletItemCookingData.class));
        ForgeEventHandler.init();

        if (FMLEnvironment.dist == Dist.CLIENT){
            ClientForgeEventHandler.init();
            modBus.addListener(this::onClientSetup);
            modBus.addListener(SkilletModels::onModelRegister);
            modBus.addListener(SkilletModels::onModelBake);
        }
    }
}

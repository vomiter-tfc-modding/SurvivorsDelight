package com.vomiter.survivorsdelight.data;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.asset.SDCabinetBlockStateProvider;
import com.vomiter.survivorsdelight.data.asset.SDLangProvider;
import com.vomiter.survivorsdelight.data.asset.SDSkilletBlockStateProvider;
import com.vomiter.survivorsdelight.data.book.content.SDBookEN;
import com.vomiter.survivorsdelight.data.heat.SDItemHeatProvider;
import com.vomiter.survivorsdelight.data.loot.SDCabinetLootTableProvider;
import com.vomiter.survivorsdelight.data.loot.SDSkilletLootTableProvider;
import com.vomiter.survivorsdelight.data.recipe.SDRecipeProvider;
import com.vomiter.survivorsdelight.data.size.SDItemSizeProvider;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
@EventBusSubscriber(modid = SurvivorsDelight.MODID)
public class DataGenerators
{
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        SDTags.gatherData(event);

        generator.addProvider(event.includeServer(), new SDRecipeProvider(output, lookupProvider, helper));
        generator.addProvider(event.includeServer(), new SDSkilletLootTableProvider(output));
        generator.addProvider(event.includeServer(), new SDCabinetLootTableProvider(output));

        generator.addProvider(event.includeClient(), new SDSkilletBlockStateProvider(output, helper));
        generator.addProvider(event.includeClient(), new SDCabinetBlockStateProvider(output, helper));

        generator.addProvider(event.includeClient(), new SDLangProvider(output, "en_us"));
        generator.addProvider(event.includeClient(), new SDLangProvider(output, "zh_tw"));

        //generator.addProvider(event.includeServer(), new SDFoodDataProvider(output, SurvivorsDelight.MODID));
        generator.addProvider(event.includeServer(), new SDItemSizeProvider(output, SurvivorsDelight.MODID));
        generator.addProvider(event.includeServer(), SurvivorsDelight.foodAndCookingGenerator.provider());
        generator.addProvider(event.includeClient(), new SDItemHeatProvider(output, SurvivorsDelight.MODID));

        SDBookEN.accept(event);

    }
}
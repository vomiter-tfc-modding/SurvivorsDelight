package com.vomiter.survivorsdelight.data.tags;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

public class SDTags {
    public static void gatherData(GatherDataEvent event){
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        ModBlockTagsProvider blockTags = new ModBlockTagsProvider(output, lookupProvider, helper);
        ModItemTagsProvider itemTags = new ModItemTagsProvider(output, lookupProvider, blockTags, helper);
        ModEntityTypeTagsProvider entityTags = new ModEntityTypeTagsProvider(output, lookupProvider, SurvivorsDelight.MODID, helper);
        ModFluidTagsProvider fluidTags = new ModFluidTagsProvider(output, lookupProvider, SurvivorsDelight.MODID, helper);

        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), itemTags);
        generator.addProvider(event.includeServer(), entityTags);
        generator.addProvider(event.includeServer(), fluidTags);
    }

    public static class FluidTags{
        public static TagKey<Fluid> create(String path){
            return TagKey.create(
                    Registries.FLUID,
                    SDUtils.RLUtils.build(SurvivorsDelight.MODID, path)
            );
        }
        public static TagKey<Fluid> COOKING_OILS = create("cooking_oils");
    }

    public static class BlockTags{
        public static TagKey<Block> create(String path){
            return TagKey.create(
                    Registries.BLOCK,
                    SDUtils.RLUtils.build(SurvivorsDelight.MODID, path)
            );
        }

        public static final TagKey<Block> FARMERS_FARMLAND = create("farmers_farmland");
        public static final TagKey<Block> FARMERS_SOIL = create("farmers_soil");


        public static final TagKey<Block> STATIC_HEAT_LOW = create("static_heat_low");
        public static final TagKey<Block> STATIC_HEAT_MODERATE = create("static_heat_moderate");
        public static final TagKey<Block> STATIC_HEAT_HIGH = create("static_heat_high");
        public static final TagKey<Block> HEAT_TO_BLOCK_BLACKLIST = create("heat_to_block_blacklist");
        public static final TagKey<Block> HEAT_TO_IN_HAND_BLACKLIST = create("heat_to_in_hand_blacklist");
        public static final TagKey<Block> SKILLETS = create("skillets");

        public static final TagKey<Block> CABINETS = create("cabinets");
        public static final TagKey<Block> FEAST_BLOCKS = create("feast_blocks");

    }

    public static class ItemTags {
        public static TagKey<Item> create(String path){
            return TagKey.create(
                    Registries.ITEM,
                    SDUtils.RLUtils.build(SurvivorsDelight.MODID, path)
            );
        }

        public static TagKey<Item> create(String namespace, String path){
            return TagKey.create(
                    Registries.ITEM,
                    SDUtils.RLUtils.build(namespace, path)
            );
        }

        public static final TagKey<Item> CABINETS = create("cabinets");

        public static final TagKey<Item> FOOD_MODEL_COATING = create("food_model_coating");
        public static final TagKey<Item> RETURN_COPPER_SKILLET = create("return_copper_skillet");
        public static final TagKey<Item> SKILLETS = create("skillets");
        public static final TagKey<Item> SKILLET_HEADS = create("skillet_heads");
        public static final TagKey<Item> UNFINISHED_SKILLETS = create("unfinished_skillets");
        public static final TagKey<Item> WOOD_PRESERVATIVES = create("wood_preservatives");
        public static final TagKey<Item> APPLE_FOR_CIDER = create("apple_for_cider");
        public static final TagKey<Item> CUT_FOOD = create("cut_food");
        public static final TagKey<Item> COCOA_POWDER = create("cocoa_powder");
        public static final TagKey<Item> PIE_CRUST_DAIRY = create("pie_crust_dairy");
        public static final TagKey<Item> FRUIT_FOR_CHEESECAKE = create("fruit_for_cheesecake");
        public static final TagKey<Item> CHEESE_FOR_CHEESECAKE = create("cheese_for_cheesecake");
        public static final TagKey<Item> CHOCOLATE_FOR_CHEESECAKE = create("chocolate_for_cheesecake");
        public static final TagKey<Item> SOUPS = create("soups");
        public static final TagKey<Item> WASHABLE = create("washable");
        public static final TagKey<Item> FOODS_WITH_STANDARD_SIZE = create("foods_with_std_size");
        public static final TagKey<Item> FISHES_USABLE_IN_STEW = create("fishes_usable_in_stew");
        public static final TagKey<Item> BOWL_MEALS = create("bowl_meals");
        public static final TagKey<Item> MEATS_FOR_SHEPHERDS_PIE = create("meats_for_shepherds_pie");
        public static final TagKey<Item> FEAST_SERVINGS = create("feast_servings");
        public static final TagKey<Item> PIE_SLICES = create("pie_slices");
        public static final TagKey<Item> SLICES_AND_SERVINGS = create("slices_and_servings");
        public static final TagKey<Item> FEAST_BLOCKS = create("feast_blocks");
        public static final TagKey<Item> PIE_BLOCKS = create("pie_blocks");
        public static final TagKey<Item> COOKED_POULTRY = create("cooked_poultry");

        public static final TagKey<Item> TFC_DOUGHS = create("c", "foods/dough");
        public static final TagKey<Item> TFC_GLASS_BOTTLES = create("tfc", "glass_bottles");
        public static final TagKey<Item> TFC_RAW_MEATS = create("c", "foods/raw_meats");
        public static final TagKey<Item> TFC_COOKED_MEATS = create("c", "foods/cooked_meats");
        public static final TagKey<Item> TFC_VEGETABLES = create("c", "foods/vegetable");
        public static final TagKey<Item> TFC_GRAINS = create("c", "foods/grain");
        public static final TagKey<Item> TFC_FRUITS = create("c", "foods/fruit");
        public static final TagKey<Item> TFC_SWEETENER = create("tfc", "foods/sweeteners");

    }

}

package com.vomiter.survivorsdelight.data.tags;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.data.food.SDBasicFoodData;
import com.vomiter.survivorsdelight.registry.SDBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider,
                               ModBlockTagsProvider blockTags,
                               ExistingFileHelper helper) {
        super(output, lookupProvider, blockTags.contentsGetter(), SurvivorsDelight.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(TFCTags.Items.COMPOST_BROWNS_HIGH).add(Items.BROWN_MUSHROOM);
        tag(TFCTags.Items.COMPOST_BROWNS).add(ModItems.STRAW.get(), ModItems.TREE_BARK.get());

        SDBlocks.CABINETS.values().forEach(c -> tag(SDTags.ItemTags.CABINETS).add(c.get().asItem()));

        addSkilletTags();
        addFoodTags();
        assert TFCItems.GLUE.getKey() != null;
        tag(SDTags.ItemTags.FOOD_MODEL_COATING).add(TFCItems.GLUE.getKey());
        tag(SDTags.ItemTags.WOOD_PRESERVATIVES).addOptional(SDUtils.RLUtils.build("firmalife", "beeswax"));
        tag(SDTags.ItemTags.RETURN_COPPER_SKILLET).add(Objects.requireNonNull(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER).getKey()));
        tag(SDTags.ItemTags.RETURN_COPPER_SKILLET).add(Objects.requireNonNull(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER_SILVER).getKey()));
        tag(SDTags.ItemTags.RETURN_COPPER_SKILLET).add(Objects.requireNonNull(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER_TIN).getKey()));

        tag(SDTags.ItemTags.WASHABLE)
                .add(ModItems.BONE_BROTH.get(), ModItems.TOMATO_SAUCE.get())
                .add(ModItems.HONEY_GLAZED_HAM_BLOCK.get(), ModItems.ROAST_CHICKEN_BLOCK.get(), ModItems.RICE_ROLL_MEDLEY_BLOCK.get(), ModItems.SHEPHERDS_PIE_BLOCK.get())
                .add(ModItems.DOG_FOOD.get())
                .addTag(SDTags.ItemTags.SOUPS)
                .addTag(SDTags.ItemTags.BOWL_MEALS)
                .addTag(SDTags.ItemTags.FEAST_SERVINGS)
                .addOptionalTag(ModTags.DRINKS);

        tag(TFCTags.Items.DOG_FOOD).add(ModItems.DOG_FOOD.get());
        tag(TFCTags.Items.HORSE_FOOD).add(ModItems.HORSE_FEED.get());
        tag(TFCTags.Items.USABLE_ON_TOOL_RACK).addTag(SDTags.ItemTags.SKILLETS);

        tag(SDTags.ItemTags.FOODS_WITH_STANDARD_SIZE)
                .add(ModItems.BONE_BROTH.get(), ModItems.TOMATO_SAUCE.get())
                .add(ModItems.PIE_CRUST.get())
                .add(ModItems.BARBECUE_STICK.get())
                .add(ModItems.DUMPLINGS.get(), ModItems.CABBAGE_ROLLS.get(), ModItems.STUFFED_POTATO.get())
                .addTag(SDTags.ItemTags.SLICES_AND_SERVINGS)
                .addTag(SDTags.ItemTags.SOUPS)
                .addTag(SDTags.ItemTags.BOWL_MEALS)
                .addOptionalTag(ModTags.DRINKS);

        tag(ModTags.SERVING_CONTAINERS)
                .add(TFCBlocks.CERAMIC_BOWL.get().asItem())
                .addOptionalTag(SDTags.ItemTags.TFC_GLASS_BOTTLES);
    }

    private void addFoodTags(){
        tag(SDTags.ItemTags.FEAST_BLOCKS).add(
                ModItems.HONEY_GLAZED_HAM_BLOCK.get(),
                ModItems.ROAST_CHICKEN_BLOCK.get(),
                ModItems.RICE_ROLL_MEDLEY_BLOCK.get(),
                ModItems.SHEPHERDS_PIE_BLOCK.get(),
                ModItems.STUFFED_PUMPKIN_BLOCK.get()
        );

        tag(SDTags.ItemTags.FEAST_SERVINGS).add(
                ModItems.SHEPHERDS_PIE.get(),
                ModItems.HONEY_GLAZED_HAM.get(),
                ModItems.STUFFED_PUMPKIN.get(),
                ModItems.ROAST_CHICKEN.get()
        );
        tag(SDTags.ItemTags.PIE_BLOCKS).add(
                ModItems.APPLE_PIE.get(),
                ModItems.SWEET_BERRY_CHEESECAKE.get(),
                ModItems.CHOCOLATE_PIE.get()
        );

        tag(SDTags.ItemTags.PIE_SLICES).add(
                ModItems.SWEET_BERRY_CHEESECAKE_SLICE.get(),
                ModItems.APPLE_PIE_SLICE.get(),
                ModItems.CHOCOLATE_PIE_SLICE.get()
        );

        tag(SDTags.ItemTags.SLICES_AND_SERVINGS)
                .addTags(SDTags.ItemTags.FEAST_SERVINGS, SDTags.ItemTags.PIE_SLICES);

        tag(SDTags.ItemTags.MEATS_FOR_SHEPHERDS_PIE).add(
                ModItems.MUTTON_CHOPS.get(),
                ModItems.MINCED_BEEF.get(),
                TFCItems.FOOD.get(Food.BEEF).get(),
                TFCItems.FOOD.get(Food.CHEVON).get(),
                TFCItems.FOOD.get(Food.MUTTON).get()
        );

        tag(SDTags.ItemTags.create("tfc", "foods/usable_in_soup")).add(Items.BROWN_MUSHROOM);

        tag(SDTags.ItemTags.BOWL_MEALS).add(
                ModItems.PASTA_WITH_MEATBALLS.get(),
                ModItems.PASTA_WITH_MUTTON_CHOP.get(),
                ModItems.ROASTED_MUTTON_CHOPS.get(),
                ModItems.VEGETABLE_NOODLES.get(),
                ModItems.STEAK_AND_POTATOES.get(),
                ModItems.RATATOUILLE.get(),
                ModItems.SQUID_INK_PASTA.get(),
                ModItems.GRILLED_SALMON.get(),
                ModItems.MUSHROOM_RICE.get()
        );

        tag(SDTags.ItemTags.FISHES_USABLE_IN_STEW).add(
                TFCItems.FOOD.get(Food.COOKED_COD).get(),
                TFCItems.FOOD.get(Food.COOKED_SALMON).get(),
                TFCItems.FOOD.get(Food.COOKED_BLUEGILL).get(),
                TFCItems.FOOD.get(Food.COOKED_TROPICAL_FISH).get(),
                TFCItems.FOOD.get(Food.COOKED_LARGEMOUTH_BASS).get(),
                TFCItems.FOOD.get(Food.COOKED_SMALLMOUTH_BASS).get(),
                TFCItems.FOOD.get(Food.COOKED_CRAPPIE).get(),
                TFCItems.FOOD.get(Food.COOKED_LAKE_TROUT).get(),
                TFCItems.FOOD.get(Food.COOKED_RAINBOW_TROUT).get()
        );

        tag(SDTags.ItemTags.SOUPS).add(
                ModItems.BACON_AND_EGGS.get(),
                ModItems.FRIED_RICE.get(),
                ModItems.CHICKEN_SOUP.get(),
                ModItems.NOODLE_SOUP.get(),
                ModItems.PUMPKIN_SOUP.get(),
                ModItems.VEGETABLE_SOUP.get(),
                ModItems.BEEF_STEW.get(),
                ModItems.FISH_STEW.get(),
                ModItems.BAKED_COD_STEW.get()
        );
        SDBasicFoodData.cutSpecs.forEach(spec -> {
            var item = spec.item().get();
            String path = Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(spec.item().get())).getPath();
            tag(SDTags.ItemTags.CUT_FOOD).add(item);
            if(item.equals(ModItems.CABBAGE_LEAF.get())) tag(SDTags.ItemTags.TFC_VEGETABLES).add(item);
            else if(path.contains("cooked")) tag(SDTags.ItemTags.TFC_COOKED_MEATS).add(item);
            else tag(SDTags.ItemTags.TFC_RAW_MEATS).add(item);
        });
        tag(SDTags.ItemTags.TFC_RAW_MEATS).add(ModItems.HAM.get());

        tag(SDTags.ItemTags.APPLE_FOR_CIDER).add(TFCItems.FOOD.get(Food.RED_APPLE).get()).add(TFCItems.FOOD.get(Food.GREEN_APPLE).get());
        tag(SDTags.ItemTags.COCOA_POWDER).addOptional(SDUtils.RLUtils.build("firmalife", "food/cocoa_powder"));
        tag(SDTags.ItemTags.PIE_CRUST_DAIRY)
                .add(TFCItems.FOOD.get(Food.CHEESE).get())
                .addOptional(SDUtils.RLUtils.build("firmalife", "food/butter"));
        List.of("tfc:food/blackberry",
                "tfc:food/blueberry",
                "tfc:food/bunchberry",
                "tfc:food/cloudberry",
                "tfc:food/cranberry",
                "tfc:food/elderberry",
                "tfc:food/gooseberry",
                "tfc:food/raspberry",
                "tfc:food/snowberry",
                "tfc:food/strawberry",
                "tfc:food/wintergreen_berry",
                "tfc:food/cherry").forEach(s -> {
                    var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(s));
                    if (item != null) tag(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE).add(item);
                });
        tag(SDTags.ItemTags.CHEESE_FOR_CHEESECAKE)
                .add(TFCItems.FOOD.get(Food.CHEESE).get())
                .addOptionalTag(SDUtils.RLUtils.build("firmalife", "foods/cheeses"));
        tag(SDTags.ItemTags.CHOCOLATE_FOR_CHEESECAKE)
                .addOptionalTag(SDUtils.RLUtils.build("firmalife", "chocolate_blends"));
        tag(SDTags.ItemTags.RAW_POULTRY)
                .add(SDUtils.getTFCFoodItem(Food.CHICKEN))
                .add(SDUtils.getTFCFoodItem(Food.DUCK))
                .add(SDUtils.getTFCFoodItem(Food.QUAIL));
        tag(SDTags.ItemTags.COOKED_POULTRY)
                .add(SDUtils.getTFCFoodItem(Food.COOKED_CHICKEN))
                .add(SDUtils.getTFCFoodItem(Food.COOKED_DUCK))
                .add(SDUtils.getTFCFoodItem(Food.COOKED_QUAIL));
        tag(SDTags.ItemTags.RAW_NUTRIENT_FOWL)
                .add(SDUtils.getTFCFoodItem(Food.CHICKEN))
                .add(SDUtils.getTFCFoodItem(Food.PEAFOWL))
                .add(SDUtils.getTFCFoodItem(Food.TURKEY));
        tag(SDTags.ItemTags.COOKED_NUTRIENT_FOWL)
                .add(SDUtils.getTFCFoodItem(Food.COOKED_CHICKEN))
                .add(SDUtils.getTFCFoodItem(Food.COOKED_PEAFOWL))
                .add(SDUtils.getTFCFoodItem(Food.COOKED_TURKEY));

    }

    private void addSkilletTags(){
        for (SkilletMaterial m : SkilletMaterial.values()){
            var skillet = SDSkilletItems.getKey(m);
            var head = SDSkilletPartItems.HEADS.get(m);
            var uf = SDSkilletPartItems.UNFINISHED.get(m);

            tag(SDTags.ItemTags.SKILLETS).add(skillet);
            if(m.isWeapon){
                tag(TFCTags.Items.DEALS_CRUSHING_DAMAGE).add(skillet);
            }
            if(m.material.contains("copper")){
                var copper_tag = tag(TagKey.create(
                        ResourceKey.createRegistryKey(SDUtils.RLUtils.build("minecraft", "item")),
                        SDUtils.RLUtils.build("tfc", "metal_item/copper"))
                ).add(skillet);
                if(head != null) {
                    assert head.getKey() != null;
                    copper_tag.add(head.getKey());
                }
                if(uf != null) {
                    assert uf.getKey() != null;
                    copper_tag.add(uf.getKey());
                }
            }
            else{
                var metal_tag = tag(TagKey.create(
                        ResourceKey.createRegistryKey(SDUtils.RLUtils.build("minecraft", "item")),
                        SDUtils.RLUtils.build("tfc", "metal_item/" + m.material)))
                        .add(skillet);
                if(head != null) {
                    assert head.getKey() != null;
                    metal_tag.add(head.getKey());
                    tag(SDTags.ItemTags.SKILLET_HEADS).add(head.getKey());
                }
                if(uf != null) {
                    assert uf.getKey() != null;
                    metal_tag.add(uf.getKey());
                    tag(SDTags.ItemTags.UNFINISHED_SKILLETS).add(uf.getKey());
                }
            }
        }

    }

}

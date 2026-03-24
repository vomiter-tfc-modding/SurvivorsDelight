package com.vomiter.survivorsdelight.data.recipe;

import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.DataGenerationHelpers;
import net.dries007.tfc.util.Metal;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Arrays;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;
import static com.vomiter.survivorsdelight.registry.SDBlocks.CABINETS;

public class SDCraftingRecipes {
    public void save(RecipeOutput out){
        misc(out);
        skillets(out);
    }

    private DataGenerationHelpers.Builder recipe(RecipeOutput out, String path) {
        return new DataGenerationHelpers.Builder((name, recipe) -> {
            ResourceLocation id = SDUtils.RLUtils.build(path);
            out.accept(id, recipe, null);
        });
    }

    public void skillets(RecipeOutput out){
        for (SkilletMaterial value : SkilletMaterial.values()){
            boolean isDefaultMetal = Arrays.stream(Metal.values()).anyMatch(m -> m.name().equals(value.name()));
            if(!isDefaultMetal && !value.equals(SkilletMaterial.CAST_IRON)) continue;
            Item unfinished = SDSkilletPartItems.UNFINISHED.get(value).get();
            Item skillet = SDSkilletItems.SKILLETS.get(value).get();
            String path = "crafting/skillet/" + value.material;
            Ingredient woodRod = Ingredient.of(SDUtils.TagUtils.itemTag("c", "rods/wooden"));

            recipe(out, path)
                    .input('U', unfinished)     // unfinished 頭
                    .input('R', woodRod)        // 木棒 tag
                    .pattern("U")
                    .pattern("R")
                    .copyForging()
                    .source(0, 0)
                    .shaped(skillet);

            if(value.equals(SkilletMaterial.STEEL)){
                recipe(out, "crafting/skillet/farmer")
                        .input('U', unfinished)     // unfinished 頭
                        .input('R', Items.BRICK)
                        .pattern("U")
                        .pattern("R")
                        .copyForging()
                        .source(0, 0)
                        .shaped(SDSkilletItems.FARMER.get());
            }
        }
    }

    public void misc(RecipeOutput out){
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HORSE_FEED.get())
                .pattern("AC")
                .pattern("SA")
                .define('A', SDTags.ItemTags.APPLE_FOR_CIDER)
                .define('C', TFCItems.FOOD.get(Food.CARROT).get())
                .define('S', TFCBlocks.THATCH.get().asItem())
                .unlockedBy("has_thatch", InventoryChangeTrigger.TriggerInstance.hasItems(TFCBlocks.THATCH.get()))
                .save(out, SDUtils.RLUtils.build("crafting/misc/horse_feed"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DOG_FOOD.get())
                .pattern(" B ")
                .pattern("MRM")
                .pattern(" b ")
                .define('B', Items.BONE)
                .define('b', Items.BOWL)
                .define('M', SDTags.ItemTags.MEATS_FOR_SHEPHERDS_PIE)
                .define('R', Items.ROTTEN_FLESH)
                .unlockedBy("has_rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
                .save(out, SDUtils.RLUtils.build("crafting/misc/dog_food"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ORGANIC_COMPOST.get())
                .requires(TFCTags.Items.COMPOST_BROWNS_HIGH)
                .requires(TFCTags.Items.COMPOST_BROWNS_HIGH)
                .requires(TFCTags.Items.COMPOST_GREENS_HIGH)
                .requires(TFCTags.Items.COMPOST_GREENS_HIGH)
                .requires(SDTags.ItemTags.create("minecraft", "dirt"))
                .requires(Items.ROTTEN_FLESH)
                .requires(TFCItems.COMPOST.get())
                .requires(TFCItems.ROTTEN_COMPOST.get())
                .unlockedBy("has_rotten_flesh", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ROTTEN_FLESH))
                .save(out, SDUtils.RLUtils.build("crafting/misc/organic_compost"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ORGANIC_COMPOST.get(), 2)
                .requires(SDTags.ItemTags.create("minecraft", "dirt"))
                .requires(Items.BROWN_MUSHROOM)
                .requires(TFCTags.Items.COMPOST_BROWNS)
                .requires(TFCTags.Items.COMPOST_GREENS)
                .requires(ModItems.RICH_SOIL.get())
                .unlockedBy("has_organic_compost", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ORGANIC_COMPOST.get()))
                .save(out, SDUtils.RLUtils.build("crafting/misc/organic_compost_with_mushroom"));
    }
    
    public void cabinetForWood(Wood wood, RecipeOutput out) {
        ItemLike result = CABINETS.get(wood).get().asItem();
        ItemLike lumber  = TFCItems.LUMBER.get(wood).get();
        ItemLike trapdoor = wood.getBlock(Wood.BlockType.TRAPDOOR).get().asItem();

        // LLL
        // T T
        // LLL
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, result)
                .pattern("LLL")
                .pattern("T T")
                .pattern("LLL")
                .define('L', lumber)
                .define('T', trapdoor)
                .group(MODID + ":cabinet")
                .unlockedBy("has_" + wood.getSerializedName() + "_lumber",
                        InventoryChangeTrigger.TriggerInstance.hasItems(lumber))
                .unlockedBy("has_" + wood.getSerializedName() + "_trapdoor",
                        InventoryChangeTrigger.TriggerInstance.hasItems(trapdoor))
                .save(out, SDUtils.RLUtils.build(
                        "crafting/cabinet/" + wood.getSerializedName()
                ));
    }
}

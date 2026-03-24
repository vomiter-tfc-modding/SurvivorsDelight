package com.vomiter.survivorsdelight.data.recipe.cutting;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.ItemAbilities;
import vectorwing.farmersdelight.common.crafting.ingredient.ItemAbilityIngredient;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.data.builder.CuttingBoardRecipeBuilder;

import java.util.Locale;

public class WoodCuttingRecipes{

    public void stripForBark(Wood wood, RecipeOutput out){
        Block log = wood.getBlock(Wood.BlockType.LOG).get();
        Block strippedLog = wood.getBlock(Wood.BlockType.STRIPPED_LOG).get();
        Block woodBlock = wood.getBlock(Wood.BlockType.WOOD).get();
        Block strippedWood = wood.getBlock(Wood.BlockType.STRIPPED_WOOD).get();

        ItemLike bark = ModItems.TREE_BARK.get();

        final String woodName = wood.getSerializedName();
        final ResourceLocation logRecipeId = SDUtils.RLUtils.build(
                SurvivorsDelight.MODID,
                "cutting/tfc/strip_wood/" + woodName + "_log"
        );
        final ResourceLocation woodRecipeId = SDUtils.RLUtils.build(
                SurvivorsDelight.MODID,
                "cutting/tfc/strip_wood/" + woodName + "_wood"
        );

        // 使用 Farmers Delight 的 CuttingBoardRecipeBuilder
        CuttingBoardRecipeBuilder
                .cuttingRecipe(
                        Ingredient.of(log),
                        new ItemAbilityIngredient(ItemAbilities.AXE_STRIP).toVanilla(),
                        strippedLog, 1
                )
                .addResult(bark, 1)
                .addSound(SoundEvents.AXE_STRIP)
                .build(out, logRecipeId);

        CuttingBoardRecipeBuilder
                .cuttingRecipe(
                        Ingredient.of(woodBlock),
                        new ItemAbilityIngredient(ItemAbilities.AXE_STRIP).toVanilla(),
                        strippedWood, 1
                )
                .addResult(bark, 1)
                .addResultWithChance(bark, 0.5f, 1)
                .addSound(SoundEvents.AXE_STRIP)
                .build(out, woodRecipeId);

    }

    public void salvageWoodFurnitureType(Wood wood, Wood.BlockType type, int count, RecipeOutput out){
        Item lumber = TFCItems.LUMBER.get(wood).get();
        TagKey<Item> sawsTag = TagKey.create(Registries.ITEM, SDUtils.RLUtils.build(TerraFirmaCraft.MOD_ID, "tools/saw"));

        CuttingBoardRecipeBuilder.cuttingRecipe(
                Ingredient.of(wood.getBlock(type).get()),
                Ingredient.of(sawsTag),
                lumber,
                count
        ).build(
                out,
                SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cutting/tfc/salvage/wood_furniture/" + wood.getSerializedName() + "_" + type.name().toLowerCase(Locale.ROOT))
        );
    }

    public void salvageHangingSign(Wood wood, Metal metal, RecipeOutput out){
        Item lumber = TFCItems.LUMBER.get(wood).get();
        Block chain = TFCBlocks.METALS.get(metal).get(Metal.BlockType.CHAIN).get();
        TagKey<Item> sawsTag = TagKey.create(Registries.ITEM, SDUtils.RLUtils.build(TerraFirmaCraft.MOD_ID, "saws"));

        CuttingBoardRecipeBuilder.cuttingRecipe(
                Ingredient.of(TFCItems.HANGING_SIGNS.get(wood).get(metal).get()),
                Ingredient.of(sawsTag),
                lumber,
                2
                )
                .addResultWithChance(chain, 0.5f, 1)
                .build(out,
                SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cutting/tfc/salvage/hanging_sign/" + wood.getSerializedName() + "_" + metal.getSerializedName())
        );

    }

    public void salvageWoodFurniture(Wood wood, RecipeOutput out){
        salvageWoodFurnitureType(wood, Wood.BlockType.DOOR, 3, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.TRAPDOOR, 2, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.FENCE, 2, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.LOG_FENCE, 2, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.FENCE_GATE, 2, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.BUTTON, 4, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.PRESSURE_PLATE, 2, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.TOOL_RACK, 6, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.WORKBENCH, 16, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.CHEST, 8, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.TRAPPED_CHEST, 8, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.LOOM, 7, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.SLUICE, 3, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.BARREL, 7, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.BOOKSHELF, 6, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.LECTERN, 10, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.SCRIBING_TABLE, 14, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.SEWING_TABLE, 24, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.AXLE, 4, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.ENCASED_AXLE, 4, out);
        salvageWoodFurnitureType(wood, Wood.BlockType.SIGN, 2, out);
    }

}

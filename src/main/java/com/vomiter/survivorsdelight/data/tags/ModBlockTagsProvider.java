package com.vomiter.survivorsdelight.data.tags;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.SDBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.registry.ModBlocks;
import vectorwing.farmersdelight.common.tag.ModTags;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput out,
                                CompletableFuture<HolderLookup.Provider> lookup,
                                @Nullable ExistingFileHelper efh) {
        super(out, lookup, SurvivorsDelight.MODID, efh);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(ModTags.TRAY_HEAT_SOURCES).add(TFCBlocks.FIREPIT.get());

        SDBlocks.CABINETS.values().forEach(c -> tag(SDTags.BlockTags.CABINETS).add(c.get()));
        tag(SDTags.BlockTags.FEAST_BLOCKS).add(
                ModBlocks.HONEY_GLAZED_HAM_BLOCK.get(),
                ModBlocks.ROAST_CHICKEN_BLOCK.get(),
                ModBlocks.RICE_ROLL_MEDLEY_BLOCK.get(),
                ModBlocks.SHEPHERDS_PIE_BLOCK.get(),
                ModBlocks.STUFFED_PUMPKIN_BLOCK.get()
        );

        TagKey<Block> STATIC_HEAT_250 = SDTags.BlockTags.STATIC_HEAT_LOW;
        TagKey<Block> STATIC_HEAT_500 = SDTags.BlockTags.STATIC_HEAT_MODERATE;
        TagKey<Block> STATIC_HEAT_1500 = SDTags.BlockTags.STATIC_HEAT_HIGH;

        for (SkilletMaterial m : SkilletMaterial.values()){
            tag(SDTags.BlockTags.SKILLETS).add(SDSkilletBlocks.getKey(m));
        }

        TFCBlocks.MAGMA_BLOCKS.forEach((r, b)->{
            assert b.getKey() != null;
            tag(STATIC_HEAT_250).add(b.getKey());
            tag(SDTags.BlockTags.HEAT_TO_BLOCK_BLACKLIST).add(b.getKey());
        });

        tag(STATIC_HEAT_500) //fire. (not very sure about the vanilla magma and campfires, but maybe it could provide some compat in modpacks.)
                .add(Blocks.FIRE)
                .add(Blocks.MAGMA_BLOCK)
                .addTag(BlockTags.CAMPFIRES);

        tag(STATIC_HEAT_1500) //Lava, lava cauldron
                .add(Blocks.LAVA)
                .add(Blocks.LAVA_CAULDRON);

        assert ModBlocks.SKILLET.getKey() != null;
        assert ModBlocks.COOKING_POT.getKey() != null;
        tag(TFCTags.Blocks.FORGE_INVISIBLE_WHITELIST)
                .add(ModBlocks.SKILLET.getKey())
                .add(ModBlocks.COOKING_POT.getKey())
                .addTag(SDTags.BlockTags.SKILLETS);

        assert TFCBlocks.CRUCIBLE.getKey() != null;
        tag(ModTags.HEAT_CONDUCTORS)
                .add(TFCBlocks.CRUCIBLE.getKey());

        tag(TFCTags.Blocks.FARMLAND)
                .add(ModBlocks.RICH_SOIL_FARMLAND.get());
    }
}

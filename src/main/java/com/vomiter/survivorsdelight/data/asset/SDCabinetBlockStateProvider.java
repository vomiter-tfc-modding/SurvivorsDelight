package com.vomiter.survivorsdelight.data.asset;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.registry.SDBlocks;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
// ⬇️ 1.21.1：Forge → NeoForge 套件名稱
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SDCabinetBlockStateProvider extends BlockStateProvider {
    private final ExistingFileHelper existingFileHelper;

    public SDCabinetBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, SurvivorsDelight.MODID, existingFileHelper);
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public @NotNull String getName() {
        return "BlockStates (Cabinets): " + SurvivorsDelight.MODID;
    }

    private void trackTexture(String pathNoExt) {
        existingFileHelper.trackGenerated(
                modLoc("block/" + pathNoExt),
                PackType.CLIENT_RESOURCES,
                ".png",
                "textures"
        );
    }

    @Override
    protected void registerStatesAndModels() {
        for (Map.Entry<Wood, ? extends net.neoforged.neoforge.registries.DeferredHolder<Block, ? extends Block>> e
                : SDBlocks.CABINETS.entrySet()) {
            Wood wood = e.getKey();
            Block block = e.getValue().get();
            registerCabinet(wood, block);
        }
    }

    private void registerCabinet(Wood wood, Block block) {
        String basePath = "planks/cabinet/" + wood.getSerializedName();

        // 追蹤紋理，避免 ExistingFileHelper 誤判遺失
        trackTexture(basePath + "_front");
        trackTexture(basePath + "_front_open");
        trackTexture(basePath + "_side");
        trackTexture(basePath + "_top");

        ModelFile closed = orientableModel(
                basePath,
                modLoc("block/" + basePath + "_front"),
                modLoc("block/" + basePath + "_side"),
                modLoc("block/" + basePath + "_top")
        );

        ModelFile open = orientableModel(
                basePath + "_open",
                modLoc("block/" + basePath + "_front_open"),
                modLoc("block/" + basePath + "_side"),
                modLoc("block/" + basePath + "_top")
        );

        // 橡木沿用 Farmer's Delight 現成模型（確保相容）
        if (wood.equals(Wood.OAK)) {
            existingFileHelper.trackGenerated(
                    SDUtils.RLUtils.build("farmersdelight", "block/oak_cabinet"),
                    PackType.CLIENT_RESOURCES,
                    ".json",
                    "models"
            );
            existingFileHelper.trackGenerated(
                    SDUtils.RLUtils.build("farmersdelight", "block/oak_cabinet_open"),
                    PackType.CLIENT_RESOURCES,
                    ".json",
                    "models"
            );

            closed = models().getExistingFile(SDUtils.RLUtils.build("farmersdelight", "block/oak_cabinet"));
            open   = models().getExistingFile(SDUtils.RLUtils.build("farmersdelight", "block/oak_cabinet_open"));

            // 物品模型：直接繼承 FD 的方塊模型
            itemModels().withExistingParent("item/" + basePath,
                    SDUtils.RLUtils.build("farmersdelight", "block/oak_cabinet"));
        } else {
            // 物品模型：繼承本模組的關聯方塊模型
            itemModels().withExistingParent("item/" + basePath, modLoc("block/" + basePath));
        }

        ModelFile finalOpen = open;
        ModelFile finalClosed = closed;

        getVariantBuilder(block).forAllStates(state -> {
            Direction dir = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
            boolean isOpen = state.getValue(BlockStateProperties.OPEN);
            int y = yFromHorizontal(dir);
            ModelFile model = isOpen ? finalOpen : finalClosed;
            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationY(y)
                    .build();
        });
    }

    private ModelFile orientableModel(String name, ResourceLocation front, ResourceLocation side, ResourceLocation top) {
        return models()
                .withExistingParent("block/" + name, mcLoc("block/orientable"))
                .texture("front", front)
                .texture("side", side)
                .texture("top", top);
    }

    private static int yFromHorizontal(Direction dir) {
        return switch (dir) {
            case NORTH -> 0;
            case EAST  -> 90;
            case SOUTH -> 180;
            case WEST  -> 270;
            default -> 0;
        };
    }
}

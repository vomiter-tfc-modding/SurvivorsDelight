package com.vomiter.survivorsdelight.data.asset;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
// ⬇️ NeoForge 1.21.1：Forge → NeoForge
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.SkilletBlock;

import java.util.Map;
import java.util.Objects;

public class SDSkilletBlockStateProvider extends BlockStateProvider {
    private final ExistingFileHelper helper;

    public SDSkilletBlockStateProvider(PackOutput out, ExistingFileHelper helper) {
        super(out, SurvivorsDelight.MODID, helper);
        this.helper = helper;
    }

    @Override
    public @NotNull String getName() {
        return "BlockStates (Skillets): " + SurvivorsDelight.MODID;
    }

    @Override
    protected void registerStatesAndModels() {
        // 共用托盤（tray）模型
        final ResourceLocation trayModelLoc = modLoc("block/skillet/tray");
        final ModelFile trayFile = models().getExistingFile(trayModelLoc);

        for (SkilletMaterial m : SkilletMaterial.values()) {
            final String name = m.material;

            final ModelFile baseBlockModel = createSkilletBlockModel(name, m.textures);

            final Block skilletBlock = SDSkilletBlocks.SKILLETS.get(m).get();
            buildMultipartState(skilletBlock, baseBlockModel, trayFile);

            createSkilletItemModels(name, m.textures);
        }
    }

    /* ---------- 方塊模型：以 survivorsdelight:block/skillet/skillet 為 parent，覆蓋貼圖 ---------- */
    private ModelFile createSkilletBlockModel(String name, Map<String, ResourceLocation> textures) {
        BlockModelBuilder builder = models()
                .withExistingParent("block/skillet/" + name, modLoc("block/skillet/skillet"));
        applyTextures(builder, textures);
        return builder;
    }

    /* ---------- BlockState（Multipart）：四向旋轉 + SUPPORT=true 時加托盤 ---------- */
    private void buildMultipartState(Block block, ModelFile baseFile, ModelFile trayFile) {
        getMultipartBuilder(block)
                .part().modelFile(baseFile).rotationY(yRot(Direction.NORTH)).addModel()
                .condition(HorizontalDirectionalBlock.FACING, Direction.NORTH).end()
                .part().modelFile(baseFile).rotationY(yRot(Direction.SOUTH)).addModel()
                .condition(HorizontalDirectionalBlock.FACING, Direction.SOUTH).end()
                .part().modelFile(baseFile).rotationY(yRot(Direction.EAST)).addModel()
                .condition(HorizontalDirectionalBlock.FACING, Direction.EAST).end()
                .part().modelFile(baseFile).rotationY(yRot(Direction.WEST)).addModel()
                .condition(HorizontalDirectionalBlock.FACING, Direction.WEST).end()
                .part().modelFile(trayFile).addModel()
                .condition(SkilletBlock.SUPPORT, true).end();
    }

    private int yRot(Direction dir) {
        return switch (dir) {
            case NORTH -> 0;
            case SOUTH -> 180;
            case EAST  -> 90;
            case WEST  -> 270;
            default    -> 0;
        };
    }

    /* ---------- 物品模型：basic + cooking + head + unfinished ---------- */
    private void createSkilletItemModels(String name, Map<String, ResourceLocation> textures) {
        // item/skillet/<name>
        ItemModelBuilder skillet = itemModels()
                .withExistingParent("item/skillet/" + name, modLoc("item/skillet/skillet"));
        applyTextures(skillet, textures);
        skillet
                .override()
                .model(new ModelFile.ExistingModelFile(modLoc("item/skillet/cooking"), helper))
                .predicate(ResourceLocation.fromNamespaceAndPath("minecraft", "cooking"), 1)
                .end();

        // item/skillet/<name>_cooking
        ItemModelBuilder cooking = itemModels()
                .withExistingParent("item/skillet/" + name + "_cooking", modLoc("item/skillet/cooking"));
        applyTextures(cooking, textures);

        // item/skillet_head/<name>
        ItemModelBuilder head = itemModels()
                .withExistingParent("item/skillet_head/" + name, modLoc("item/skillet/skillet_head"));
        applyTextures(head, textures);

        // item/unfinished_skillet/<name> 以 item/skillet/<name> 為父
        ItemModelBuilder unfinished = itemModels()
                .withExistingParent("item/unfinished_skillet/" + name, modLoc("item/skillet/" + name));
        applyTextures(unfinished, textures);

        unfinished.texture("2", textures.getOrDefault("0", SDUtils.RLUtils.build("tfc", "block/empty")));
    }

    private <T extends ModelBuilder<T>> void applyTextures(ModelBuilder<T> builder, Map<String, ResourceLocation> textures) {
        textures.forEach((key, rl) -> {
            helper.trackGenerated(rl, PackType.CLIENT_RESOURCES, ".png", "textures");
            builder.texture(key, rl);
            if (Objects.equals(key, "0")) {
                builder.texture("particle", rl);
            }
        });
    }
}

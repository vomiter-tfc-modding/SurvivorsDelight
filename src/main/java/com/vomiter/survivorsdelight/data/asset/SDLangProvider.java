package com.vomiter.survivorsdelight.data.asset;

import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.SDBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
// Forge → NeoForge
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Objects;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;

public class SDLangProvider extends LanguageProvider {
    private final String locale;

    public SDLangProvider(PackOutput output, String locale) {
        super(output, MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        var tfcEn = ForeignLangReader.load(TerraFirmaCraft.MOD_ID, "en_us");
        var tfcZh = ForeignLangReader.load(TerraFirmaCraft.MOD_ID, "zh_tw");
        var tfc   = "zh_tw".equals(locale) ? tfcZh : tfcEn;

        // ——— Skillet 系列 ———
        for (SkilletMaterial material : SkilletMaterial.values()) {
            String tfcKey    = "metal.tfc." + material.material;
            String metalName = tfc.get(tfcKey);
            if (metalName == null) continue;

            var blockId = BuiltInRegistries.BLOCK.getKey(SDSkilletBlocks.SKILLETS.get(material).get());
            var headId  = Objects.requireNonNull(SDSkilletPartItems.HEADS.get(material).getId());
            var ufId    = Objects.requireNonNull(SDSkilletPartItems.UNFINISHED.get(material).getId());

            String blockKey = Util.makeDescriptionId("block", blockId);
            String headKey  = Util.makeDescriptionId("item", headId);
            String ufKey    = Util.makeDescriptionId("item", ufId);

            if ("en_us".equals(locale)) {
                add(blockKey, metalName + " Skillet");
                add(headKey, metalName + " Skillet Head");
                add(ufKey, "Unfinished " + metalName + " Skillet");
            } else if ("zh_tw".equals(locale)) {
                add(blockKey, "平底" + metalName + "鍋");
                add(headKey, "平底" + metalName + "鍋身");
                add(ufKey, "未完成的平底" + metalName + "鍋");
            }
        }

        // ——— 櫥櫃（各木種） ———
        for (Wood wood : Wood.values()) {
            String logKey = "block.tfc.wood.log." + wood.getSerializedName();
            String woodLogName = tfc.get(logKey);
            if (woodLogName == null) continue;

            String woodName = woodLogName.replace(" Log", "").replace("原木", "");
            String cabinetName = woodName + ("zh_tw".equals(locale) ? "櫥櫃" : " Cabinet");

            var cabId = SDBlocks.CABINETS.get(wood).getId();
            if (cabId == null) continue;

            String cabinetKey = Util.makeDescriptionId("block", cabId);
            add(cabinetKey, cabinetName);
        }
    }
}

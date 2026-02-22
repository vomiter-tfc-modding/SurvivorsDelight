package com.vomiter.survivorsdelight.data.asset;

import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.SDBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletBlocks;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.minecraft.Util;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.Objects;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;

public class SDLangProvider extends LanguageProvider {
    private final String locale; // 自己存語系


    public SDLangProvider(PackOutput output, String locale) {
        super(output, MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        var tfcEn = ForeignLangReader.load(TerraFirmaCraft.MOD_ID, "en_us");
        var tfcZh = ForeignLangReader.load(TerraFirmaCraft.MOD_ID, "zh_tw");
        var tfc   = "zh_tw".equals(locale) ? tfcZh : tfcEn;

        for (SkilletMaterial material : SkilletMaterial.values()) {
            String tfcKey   = "metal.tfc." + material.material;
            String metalName = tfc.get(tfcKey);
            if (metalName == null) continue;

            var id = Objects.requireNonNull(SDSkilletBlocks.SKILLETS.get(material).getId());
            String blockKey = Util.makeDescriptionId("block", id); // = "block.<modid>.<path>"
            String headKey = Util.makeDescriptionId("item", SDSkilletPartItems.HEADS.get(material).getId());
            String ufKey = Util.makeDescriptionId("item", SDSkilletPartItems.UNFINISHED.get(material).getId());

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

        for (Wood wood : Wood.values()) {
            String woodLogName = tfc.get("block.tfc.wood.log." + wood.getSerializedName());
            String woodName = woodLogName.replace(" Log", "").replace("原木", "");
            String cabinetName = woodName + ("zh_tw".equals(locale) ? "櫥櫃" : " Cabinet");
            String cabinetKey = Util.makeDescriptionId("block", SDBlocks.CABINETS.get(wood).getId());
            add(cabinetKey, cabinetName);
        }

    }
}

package com.vomiter.survivorsdelight.data.book;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.TerraFirmaCraft;
import net.minecraft.resources.ResourceLocation;

final class SDPatchouliConstants {
    static final String MODID = TerraFirmaCraft.MOD_ID;
    static final String BOOK_ID = "field_guide"; // the book folder id
    static final String LANG = "en_us"; // Patchouli language folder

    static ResourceLocation bookFolderRL() {
        return SDUtils.RLUtils.build(MODID, "patchouli_books/" + BOOK_ID);
    }
}
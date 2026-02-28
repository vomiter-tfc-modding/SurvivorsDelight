package com.vomiter.survivorsdelight.compat.tfc_ie;

import com.nmagpie.tfc_ie_addon.config.Config;

public class TFCIEAddonHelper {
    public static int getFurnaceTemperature(){
        return Config.SERVER.crucibleExternalHeaterTemperature.get();
    }
}

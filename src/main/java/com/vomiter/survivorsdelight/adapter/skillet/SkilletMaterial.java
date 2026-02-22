package com.vomiter.survivorsdelight.adapter.skillet;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.blocks.wood.Wood;
import net.dries007.tfc.util.Metal;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum SkilletMaterial {
    COPPER(plank(Wood.HICKORY)),
    COPPER_SILVER(plank(Wood.HICKORY)),
    COPPER_TIN(plank(Wood.HICKORY)),
    CAST_IRON(plank(Wood.OAK)),
    STEEL(5.75f, 1, plank(Wood.CHESTNUT)),
    BLACK_STEEL(7, 1.5f, plank(Wood.DOUGLAS_FIR)),
    RED_STEEL(9, 2f,
            SDUtils.RLUtils.build(TerraFirmaCraft.MOD_ID, "block/devices/crucible/side")),
    BLUE_STEEL(9, 2f,
            SDUtils.RLUtils.build(TerraFirmaCraft.MOD_ID, "block/devices/crucible/side"));
    public final String material;
    public final int durability;
    public final boolean isWeapon;
    public final float attackDamage;
    public final float attackKnockback;
    public final ResourceLocation handle;
    public final Map<String, ResourceLocation> textures = new HashMap<>();
    public final Metal.Default metal;

    SkilletMaterial(ResourceLocation handle) {
        material = name().toLowerCase(Locale.ROOT);
        metal = Metal.Default.valueOf(material.startsWith("copper")? "COPPER": name());
        durability = material.equals("copper")? 1: !metal.hasTools()? 600: metal.toolTier().getUses();
        isWeapon = false;
        attackDamage = 0;
        attackKnockback = 0;
        this.handle = handle;
        fillTextures();
    }

    SkilletMaterial(float attackDamage, float attackKnockback, ResourceLocation handle) {
        material = name().toLowerCase(Locale.ROOT);
        metal = Metal.Default.valueOf(
                material.startsWith("copper")? "COPPER": name()
        );
        durability = material.equals("copper")? 1: !metal.hasTools()? 600: metal.toolTier().getUses();
        this.isWeapon = true;
        this.attackDamage = attackDamage;
        this.attackKnockback = attackKnockback;
        this.handle = handle;
        fillTextures();
    }

    public String path() { return "skillet/" + material; }
    public ResourceLocation location() { return SDUtils.RLUtils.build(SurvivorsDelight.MODID, path()); }

    public String path_head(){ return "skillet_head/" + material;}
    public String path_uf(){ return "unfinished_skillet/" + material;}

    private static ResourceLocation plank(Wood wood){
        return SDUtils.RLUtils.build(TerraFirmaCraft.MOD_ID, "block/" + Wood.BlockType.PLANKS.nameFor(wood));
    }

    private static ResourceLocation smooth_metal(Metal.Default metal){
        return SDUtils.RLUtils.build(TerraFirmaCraft.MOD_ID, "block/metal/smooth/" + metal.getSerializedName());
    }



    private void fillTextures() {
        String metal0, metal1;

        if (material.startsWith("copper_")) {
            metal0 = "copper";
            metal1 = material.substring("copper_".length()); // "silver" / "tin"
        } else {
            metal0 = material;
            metal1 = material;
        }

        textures.put("0", smooth_metal(Metal.Default.valueOf(metal0.toUpperCase(Locale.ROOT))));
        textures.put("1", smooth_metal(Metal.Default.valueOf(metal1.toUpperCase(Locale.ROOT))));
        textures.put("2", handle);
    }
}


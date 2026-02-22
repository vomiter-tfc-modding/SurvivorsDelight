package com.vomiter.survivorsdelight.mixin.compat.almostunified;

import com.almostreliable.unified.AlmostUnifiedRuntimeImpl;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = AlmostUnifiedRuntimeImpl.class, remap = false)
public abstract class AlmostUnifiedRuntimeImpl_SDRecipeFilterMixin {

    private static final AtomicBoolean SD$ONCE = new AtomicBoolean(false);

    @Inject(method = "run(Ljava/util/Map;Z)V", at = @At("TAIL"), require = 0)
    private void sd$filterRecipesAfterAU(Map<ResourceLocation, JsonElement> recipes, boolean skipClientTracking, CallbackInfo ci) {
        // 印一次就好，避免噴 log
        if (SD$ONCE.compareAndSet(false, true)) {
            SurvivorsDelight.LOGGER.info("[SD][AU-HOOK] AlmostUnifiedRuntimeImpl.run TAIL reached. recipes={}", recipes.size());
        }

        int removed = 0;

        // 範例策略：直接封鎖 farmersdelight namespace 的 crafting/cutting/cooking
        // 你可替換成更細的 JSON 判斷（見下方 helper）
        Iterator<Map.Entry<ResourceLocation, JsonElement>> it = recipes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<ResourceLocation, JsonElement> e = it.next();
            ResourceLocation id = e.getKey();

            // 1) 最粗暴：直接封整個 FD
            // if ("farmersdelight".equals(id.getNamespace())) { it.remove(); removed++; continue; }

            // 2) 比較保守：只封 recipe type 是 crafting / FD cutting / FD cooking
            JsonElement je = e.getValue();
            if (!je.isJsonObject()) continue;
            JsonObject obj = je.getAsJsonObject();

            if (sd$isBlockedByJson(id, obj)) {
                it.remove();
                removed++;
            }
        }

        if (removed > 0) {
            SurvivorsDelight.LOGGER.info("[SD][AU-HOOK] Removed {} recipes after AU transform. remaining={}", removed, recipes.size());
        }
    }

    /**
     * - 依 id（namespace/path）
     * - 依 type（obj.get("type")）
     * - 依 result/ingredient namespace
     */
    private boolean sd$isBlockedByJson(ResourceLocation id, JsonObject obj) {
        // 範例：只針對 crafting + FD 的 cooking/cutting
        // 注意：type 可能不存在（某些 mod 自訂），要防呆
        List<String> recipesToBlockFirst = List.of(
                "integration/create/mixing/cabbage_slice_from_mixing",
                "integration/create/mixing/pie_crust_from_mixing",
                "integration/create/mixing/tomato_sauce_from_mixing"
        );
        if(recipesToBlockFirst.contains(id.getPath())) return true;

        String type = obj.has("type") ? obj.get("type").getAsString() : "";

        boolean isCrafting = "minecraft:crafting_shaped".equals(type) || "minecraft:crafting_shapeless".equals(type);
        boolean isFDCooking = "farmersdelight:cooking".equals(type);
        boolean isFDCutting = "farmersdelight:cutting".equals(type);

        if (!(isCrafting || isFDCooking || isFDCutting)) return false;
        List<String> recipesToBlock = List.of(
                "cooking/bone_broth",
                "cooking/vegetable_noodles",
                "cooking/mushroom_rice",
                "cooking/cooked_rice",
                "cooking/dumplings",
                "cooking/vegetable_soup",
                "cooking/mushroom_stew",
                "cooking/pasta_with_meatballs",
                "cooking/beetroot_soup",
                "cooking/pasta_with_mutton_chop",
                "cooking/ratatouille",
                "cooking/baked_cod_stew",
                "cooking/squid_ink_pasta",
                "cooking/rabbit_stew",
                "cooking/cabbage_rolls",
                "cooking/beef_stew",
                "cooking/fried_rice",
                "cooking/stuffed_pumpkin_block",
                "cooking/tomato_sauce",
                "cooking/chicken_soup",
                "cooking/glow_berry_custard",
                "cooking/dog_food",
                "cooking/pumpkin_soup",
                "cooking/apple_cider",
                "cooking/noodle_soup",
                "cooking/fish_stew",
                "potato_from_crate",
                "barbecue_stick",
                "stuffed_potato",
                "bacon_and_eggs",
                "egg_sandwich",
                "kelp_roll",
                "carrot_from_crate",
                "cabbage_from_leaves",
                "salmon_roll",
                "melon_popsicle",
                "steak_and_potatoes",
                "mixed_salad",
                "nether_salad",
                "roasted_mutton_chops",
                "mutton_wrap",
                "melon_juice",
                "grilled_salmon",
                "wheat_dough_from_egg",
                "honey_glazed_ham_block",
                "fruit_salad",
                "bacon_sandwich",
                "onion",
                "honey_cookie",
                "sweet_berry_cookie",
                "horse_feed",
                "cabbage",
                "chicken_sandwich",
                "canvas",
                "cod_roll",
                "tomato",
                "hamburger",
                "stove",
                "skillet",
                "beetroot_from_crate",
                "shepherds_pie_block",
                "pie_crust",
                "roast_chicken_block",
                "organic_compost_from_rotten_flesh",
                "organic_compost_from_tree_bark",
                "cutting/beef",
                "cutting/cabbage",
                "cutting/smoked_ham",
                "cutting/pumpkin",
                "cutting/wild_carrots",
                "cutting/cooked_mutton",
                "cutting/chicken",
                "cutting/cooked_cod",
                "cutting/wild_onions",
                "cutting/cooked_salmon",
                "cutting/melon",
                "cutting/salmon",
                "cutting/tag_dough",
                "cutting/mutton",
                "cutting/apple_pie",
                "cutting/cod",
                "cutting/wild_potatoes",
                "cutting/cooked_chicken",
                "cutting/ham",
                "cutting/cake",
                "cutting/sweet_berry_cheesecake",
                "cutting/kelp_roll",
                "cutting/chocolate_pie",
                "cutting/porkchop"
        );
        if(SurvivorsDelight.MODID.equals(id.getNamespace())){
            if("crafting/tfc_straw2fd_straw".equals(id.getPath())) return true;
            else if("crafting/fd_straw2tfc_straw".equals(id.getPath())) return true;
            return false;
        }
        else if("farmersdelight".equals(id.getNamespace())){
            if(id.getPath() == null) return false;
            return recipesToBlock.contains(id.getPath());
        }

        return false;
    }
}
package com.vomiter.survivorsdelight.data.recipe;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.common.recipes.WeldingRecipe;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.Metal;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Arrays;
import java.util.List;

public class SDAnvilAndWeldingRecipes {
    static List<ForgeRule> skilletRules = List.of(
            ForgeRule.BEND_THIRD_LAST,
            ForgeRule.HIT_SECOND_LAST,
            ForgeRule.DRAW_LAST
    );


    public void save(RecipeOutput out) {
        skilletRecipes(out);
        liningRecipes(out);
    }

    private void liningRecipes(RecipeOutput out){
        List<Metal> liningMaterial = List.of(Metal.TIN, Metal.SILVER);
        liningMaterial.forEach(m -> {
            var lining = SDSkilletPartItems.LININGS.get(m).get();
            AnvilRecipe anvilRecipe = new AnvilRecipe(
                    Ingredient.of(TFCItems.METAL_ITEMS.get(m).get(Metal.ItemType.INGOT).get()),
                    0,
                    List.of(ForgeRule.BEND_ANY, ForgeRule.BEND_ANY, ForgeRule.BEND_ANY),
                    false,
                    ItemStackProvider.of(new ItemStack(lining, 4))
            );
            out.accept(
                    SDUtils.RLUtils.build("anvil/skillet_lining/" + m.getSerializedName()),
                    anvilRecipe,
                    null
            );

            SkilletMaterial skilletMaterial = SkilletMaterial.valueOf("COPPER_" + m.name());
            WeldingRecipe weldingRecipe = new WeldingRecipe(
                    Ingredient.of(lining),
                    Ingredient.of(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER).get()),
                    0,
                    ItemStackProvider.of(SDSkilletItems.SKILLETS.get(skilletMaterial).get()),
                    WeldingRecipe.Behavior.IGNORE
            );
            out.accept(
                    SDUtils.RLUtils.build("welding/skillet/" + skilletMaterial.material),
                    weldingRecipe,
                    null
            );
        });
    }

    private void skilletRecipes(RecipeOutput out) {
        var wIronDS = SDUtils.TagUtils.itemTag("c", "double_sheets/wrought_iron");
        out.accept(
                ResourceLocation.fromNamespaceAndPath(SurvivorsDelight.MODID, "anvil/cooking_pot"),
                new AnvilRecipe(
                        Ingredient.of(wIronDS),
                        Metal.WROUGHT_IRON.tier(),
                        skilletRules,
                        false,
                        ItemStackProvider.of(ModItems.COOKING_POT.get())
                ),
                null
        );

        for (SkilletMaterial value : SkilletMaterial.values()) {
            TagKey<Item> doubleSheet = SDUtils.TagUtils.itemTag("c", "double_sheets/" + value.material);
            Item skillet = SDSkilletItems.get(value).get();
            boolean isDefaultMetal = Arrays.stream(Metal.values()).anyMatch(m -> m.name().equals(value.name()));
            if(!isDefaultMetal && !value.equals(SkilletMaterial.CAST_IRON)) continue;
            Metal defaultMetal = isDefaultMetal? Metal.valueOf(value.name()) : Metal.COPPER;
            Item skilletHead = SDSkilletPartItems.HEADS.get(value).get();
            int tier = Math.max(defaultMetal.tier(), 1);
            AnvilRecipe headRecipe = new AnvilRecipe(
                    Ingredient.of(doubleSheet),
                    tier,
                    skilletRules,
                    isDefaultMetal,       // applyForgingBonus -> JSON "apply_bonus": true
                    ItemStackProvider.of(skilletHead)
            );

            out.accept(
                    SDUtils.RLUtils.build("anvil/skillet_head/" + value.material),
                    headRecipe,
                    null
            );

            WeldingRecipe weldingRecipe = new WeldingRecipe(
                Ingredient.of(skilletHead),
                    Ingredient.of(TFCItems.METAL_ITEMS.get(defaultMetal).get(Metal.ItemType.ROD).get()),
                    tier - 1,
                    ItemStackProvider.of(SDSkilletPartItems.UNFINISHED.get(value).get()),
                    WeldingRecipe.Behavior.COPY_BEST
            );
            out.accept(
                    SDUtils.RLUtils.build("welding/unfinished_skillet/" + value.material),
                    weldingRecipe,
                    null
            );

            AnvilRecipe repairRecipe = new AnvilRecipe(
                Ingredient.of(skillet),
                    tier,
                    skilletRules,
                    true,
                    ItemStackProvider.of(skillet)
            );
            out.accept(
                    SDUtils.RLUtils.build("anvil/skillet_repair/" + value.material),
                    repairRecipe,
                    null
            );

        }
    }
}
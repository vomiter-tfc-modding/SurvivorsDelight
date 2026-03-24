package com.vomiter.survivorsdelight.data.size;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.size.Size;
import net.dries007.tfc.common.component.size.Weight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.tag.ModTags;

public class FDSizeData {
    final SDItemSizeProvider provider;
    public FDSizeData(SDItemSizeProvider provider){
        this.provider = provider;
    }

    void save(){
        cutFood();
        pie();
        drink();
        skillet();
        provider.newEntry(SDUtils.RLUtils.build("food/standard"))
                .ingredient(SDTags.ItemTags.FOODS_WITH_STANDARD_SIZE)
                .size(Size.SMALL)
                .weight(Weight.LIGHT)
                .save();
        provider.newEntry(SDUtils.RLUtils.build("food/feast_block"))
                .ingredient(SDTags.ItemTags.FEAST_BLOCKS)
                .size(Size.VERY_LARGE)
                .weight(Weight.VERY_HEAVY)
                .save();
        provider.newEntry(SDUtils.RLUtils.build("pet_food"))
                .ingredient(Ingredient.of(ModItems.DOG_FOOD.get(), ModItems.HORSE_FEED.get()))
                .size(Size.SMALL)
                .weight(Weight.LIGHT)
                .save();
    }

    void skillet(){
        provider.newEntry(SDUtils.RLUtils.build("skillets"))
                .ingredient(SDTags.ItemTags.SKILLETS)
                .size(Size.VERY_LARGE)
                .weight(Weight.VERY_HEAVY)
                .save();
        provider.newEntry(SDUtils.RLUtils.build("skillet_heads"))
                .ingredient(SDTags.ItemTags.SKILLET_HEADS)
                .size(Size.LARGE)
                .weight(Weight.HEAVY)
                .save();
        provider.newEntry(SDUtils.RLUtils.build("unfinished_skillets"))
                .ingredient(SDTags.ItemTags.UNFINISHED_SKILLETS)
                .size(Size.LARGE)
                .weight(Weight.HEAVY)
                .save();
    }

    void drink(){
        provider.newEntry(SDUtils.RLUtils.build("food/drinks"))
                .ingredient(ModTags.DRINKS)
                .size(Size.LARGE)
                .weight(Weight.HEAVY)
                .save();
    }

    void pie(){
        provider.newEntry(SDUtils.RLUtils.build("food/pie"))
                .ingredient(ModItems.APPLE_PIE.get(), ModItems.CHOCOLATE_PIE.get(), ModItems.SWEET_BERRY_CHEESECAKE.get())
                .size(Size.LARGE)
                .weight(Weight.MEDIUM)
                .save();
        provider.newEntry(SDUtils.RLUtils.build("food/pie_slice"))
                .ingredient(ModItems.APPLE_PIE_SLICE.get(), ModItems.CHOCOLATE_PIE_SLICE.get(), ModItems.SWEET_BERRY_CHEESECAKE_SLICE.get())
                .size(Size.SMALL)
                .weight(Weight.LIGHT)
                .save();
    }

    void cutFood(){
        provider.newEntry(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "food/cut_food"))
                .ingredient(SDTags.ItemTags.CUT_FOOD)
                .size(Size.VERY_SMALL)
                .weight(Weight.VERY_LIGHT).save();
        provider.newEntry(SDUtils.RLUtils.build("food/raw_pasta"))
                .ingredient(ModItems.RAW_PASTA.get())
                .size(Size.SMALL)
                .weight(Weight.LIGHT).save();
        provider.newEntry(SDUtils.RLUtils.build("food/ham"))
                .ingredient(ModItems.HAM.get(), ModItems.SMOKED_HAM.get())
                .weight(Weight.MEDIUM)
                .size(Size.LARGE).save();
        provider.newEntry(SDUtils.RLUtils.build("food/fish_rolls"))
                .ingredient(ModItems.SALMON_ROLL.get(), ModItems.COD_ROLL.get())
                .size(Size.SMALL)
                .weight(Weight.LIGHT).save();
    }
}

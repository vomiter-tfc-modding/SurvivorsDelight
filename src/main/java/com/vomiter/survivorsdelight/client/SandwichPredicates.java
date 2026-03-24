package com.vomiter.survivorsdelight.client;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.registry.ModItems;

public class SandwichPredicates {
    private static ResourceLocation id(){
        return SDUtils.RLUtils.build(SurvivorsDelight.MODID, "special_sandwich");
    }
    static float EGG = 0.3f;
    static float BACON = 0.6f;
    static float CHICKEN = 0.9f;

    public static void addPredicate(){
        final Item bread = TFCItems.FOOD.get(Food.WHEAT_BREAD_SANDWICH).get();
        ItemProperties.register(
                bread,
                id(),
                (stack, level, entity, seed)->{
                    var ingredientsComponent = stack.get(TFCComponents.INGREDIENTS.get());
                    if(ingredientsComponent == null) return 0;
                    int eggCount = 0;
                    int chickenCount = 0;
                    int cabbageCount = 0;
                    int carrotCount = 0;
                    int baconCount = 0;
                    int tomatoCount = 0;

                    for (ItemStack ingredient : ingredientsComponent.contents()) {
                        if(ingredient.is(SDUtils.TagUtils.itemTag("c", "eggs"))) eggCount++;
                        if(ingredient.is(TFCItems.FOOD.get(Food.COOKED_CHICKEN).get())
                                || ingredient.is(ModItems.COOKED_CHICKEN_CUTS.get())
                        ) chickenCount++;
                        else if (ingredient.is(TFCItems.FOOD.get(Food.CABBAGE).get())
                                || ingredient.is(ModItems.CABBAGE_LEAF.get())
                        ) cabbageCount++;
                        else if (ingredient.is(TFCItems.FOOD.get(Food.CARROT).get()))
                            carrotCount++;
                        else if(ingredient.is(TFCItems.FOOD.get(Food.TOMATO).get()))
                            tomatoCount++;
                        else if(ingredient.is(ModItems.COOKED_BACON.get()))
                            baconCount++;
                    }
                    if(eggCount >= 2) return EGG + 0.1f;
                    if(chickenCount == 1 && cabbageCount == 1 && carrotCount == 1) return CHICKEN + 0.1f;
                    if(baconCount == 1 && tomatoCount == 1 && cabbageCount == 1) return BACON + 0.1f;
                    return 0;
                }
        );
    }
}

package com.vomiter.survivorsdelight.client;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.TFCComponents;
import net.dries007.tfc.common.component.food.Nutrient;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.common.registry.ModItems;

public class SaladPredicates {
    private static ResourceLocation id(){
        return SDUtils.RLUtils.build(SurvivorsDelight.MODID, "special_salad");
    }

    public static void addPredicate(){
        final Item vegetableSalad = TFCItems.SALADS.get(Nutrient.VEGETABLES).get();
        ItemProperties.register(
                vegetableSalad,
                id(),
                (stack, level, entity, seed)->{
                    var ingredientsComponent = stack.get(TFCComponents.INGREDIENTS.get());
                    if(ingredientsComponent == null) return 0;
                    int cabbageCount = 0;
                    int tomatoCount = 0;
                    int beetCount = 0;
                    for (ItemStack ingredient : ingredientsComponent.contents()) {
                        if (ingredient.is(TFCItems.FOOD.get(Food.CABBAGE).get()) || ingredient.is(ModItems.CABBAGE_LEAF.get())) cabbageCount++;
                        else if(ingredient.is(TFCItems.FOOD.get(Food.TOMATO).get())) tomatoCount ++;
                        else if(ingredient.is(TFCItems.FOOD.get(Food.BEET).get())) beetCount++;
                    }
                    if(cabbageCount > 0 && tomatoCount > 0 && beetCount > 0) return 1;
                    return 0;
                }
        );

        final Item fruitSalad = TFCItems.SALADS.get(Nutrient.FRUIT).get();
        ItemProperties.register(
                fruitSalad,
                id(),
                (stack, level, entity, seed)->{
                    var ingredientsComponent = stack.get(TFCComponents.INGREDIENTS.get());
                    if(ingredientsComponent == null) return 0;
                    int appleCount = 0;
                    int melonCount = 0;
                    int berryCount = 0;
                    int pumpkinCount = 0;

                    for (ItemStack ingredient : ingredientsComponent.contents()) {
                        if(ingredient.is(SDTags.ItemTags.APPLE_FOR_CIDER)) appleCount++;
                        else if(ingredient.is(TFCItems.FOOD.get(Food.MELON_SLICE).get())) melonCount++;
                        else if(ingredient.is(SDTags.ItemTags.FRUIT_FOR_CHEESECAKE)) berryCount++;
                        else if(ingredient.is(TFCItems.FOOD.get(Food.PUMPKIN_CHUNKS).get())) pumpkinCount++;
                    }
                    if(
                            appleCount > 0
                            && melonCount > 0
                            && berryCount > 0
                            && pumpkinCount > 0
                    ) return 1;
                    return 0;
                }
        );
    }
}

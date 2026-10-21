package com.vomiter.survivorsdelight.client.model;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.dries007.tfc.common.items.Food;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = SurvivorsDelight.MODID, value = Dist.CLIENT)
public final class MealModelEvents {
    private static final List<ResourceLocation> TARGET_ITEMS = targetItems();

    private MealModelEvents() {}

    private static List<ResourceLocation> targetItems() {
        List<ResourceLocation> targets = new ArrayList<>();
        targets.add(TFCItems.FOOD.get(Food.WHEAT_BREAD_SANDWICH).getId());
        TFCItems.SOUPS.values().forEach(item -> targets.add(item.getId()));
        TFCItems.SALADS.values().forEach(item -> targets.add(item.getId()));
        return List.copyOf(targets);
    }

    @SubscribeEvent
    public static void registerAdditional(ModelEvent.RegisterAdditional event) {
        // ModelBakery fires this during every model reload, before parents/materials are collected.
        // Read here rather than relying on ordering between independently asynchronous reload listeners.
        MealModelManager.INSTANCE.reload(Minecraft.getInstance().getResourceManager());
        MealModelManager.INSTANCE.getRules().forEach(rule -> event.register(ModelResourceLocation.standalone(rule.model())));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void modifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        BakedModel missing = models.get(ModelBakery.MISSING_MODEL_VARIANT);
        List<DynamicMealModel.Binding> bindings = new ArrayList<>();
        for (MealModelRule rule : MealModelManager.INSTANCE.getRules()) {
            BakedModel model = models.get(ModelResourceLocation.standalone(rule.model()));
            if (model == null || model == missing) {
                SurvivorsDelight.LOGGER.error("Meal model rule {} references unavailable model {}", rule.id(), rule.model());
                continue;
            }
            bindings.add(new DynamicMealModel.Binding(rule, model));
        }
        for (ResourceLocation item : TARGET_ITEMS) {
            ModelResourceLocation inventory = ModelResourceLocation.inventory(item);
            BakedModel original = models.get(inventory);
            if (original != null && original != missing) models.put(inventory, new DynamicMealModel(original, bindings));
        }
    }
}

package com.vomiter.survivorsdelight.client;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import vectorwing.farmersdelight.client.model.SkilletModel;

import java.util.Map;

public class SkilletModels {
    public static void onModelRegister(ModelEvent.RegisterAdditional event) {
        for (SkilletMaterial m : SkilletMaterial.values()){
            event.register(new ModelResourceLocation(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "skillet/" + m.material +"_cooking"), "inventory"));
        }
        event.register(new ModelResourceLocation(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "skillet/" + "farmer" +"_cooking"), "inventory"));
    }

    public static void makeModel(String name, ModelEvent.ModifyBakingResult event){
        Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
        ModelResourceLocation skilletLocation = new ModelResourceLocation(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "skillet/" + name), "inventory");
        BakedModel skilletModel = modelRegistry.get(skilletLocation);
        ModelResourceLocation skilletCookingLocation = new ModelResourceLocation(SDUtils.RLUtils.build(SurvivorsDelight.MODID, "skillet/" + name +"_cooking"), "inventory");
        BakedModel skilletCookingModel = modelRegistry.get(skilletCookingLocation);
        modelRegistry.put(skilletLocation, new SkilletModel(event.getModelBakery(), skilletModel, skilletCookingModel));
    }

    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        for (SkilletMaterial m : SkilletMaterial.values()){
            makeModel(m.material, event);
        }
        makeModel("farmer", event);
    }
}

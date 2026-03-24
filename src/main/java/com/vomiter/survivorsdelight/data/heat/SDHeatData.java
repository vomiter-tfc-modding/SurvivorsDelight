package com.vomiter.survivorsdelight.data.heat;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.SDItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import net.dries007.tfc.util.Metal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class SDHeatData {
    final SDItemHeatProvider provider;
    public SDHeatData(SDItemHeatProvider provider){
        this.provider = provider;
    }
    float heat250 = 7.143f;
    float heat200 = 5.714f;

    void save() {
        for (SkilletMaterial material : SkilletMaterial.values()) {
            var skillet = SDSkilletItems.get(material).get();
            var defaultMetal = material.getDefault();
            var id = ResourceLocation.fromNamespaceAndPath("tfc", "tfc/item_heat/" + defaultMetal.getSerializedName() + "/ingot");
            var builtInHeat = BuiltinHeatJson.readBuiltinHeatTemps(SurvivorsDelight.class, id);
            provider.newEntry(BuiltInRegistries.ITEM.getKey(skillet))
                    .ingredient(skillet)
                    .heatCapacity(heat250)
                    .forgingTemperature(builtInHeat.get().forging())
                    .weldingTemperature(builtInHeat.get().welding())
                    .save();
            if(defaultMetal.equals(Metal.STEEL)){
                provider.newEntry(BuiltInRegistries.ITEM.getKey(SDSkilletItems.FARMER.get()))
                        .ingredient(SDSkilletItems.FARMER.get())
                        .heatCapacity(heat250)
                        .forgingTemperature(builtInHeat.get().forging())
                        .weldingTemperature(builtInHeat.get().welding())
                        .save();
            }
            var unfinishedSupplier = SDSkilletPartItems.UNFINISHED.get(material);
            if(unfinishedSupplier != null){
                var unfinished = unfinishedSupplier.get();
                provider.newEntry(BuiltInRegistries.ITEM.getKey(unfinished))
                        .ingredient(unfinished)
                        .heatCapacity(heat250)
                        .forgingTemperature(builtInHeat.get().forging())
                        .weldingTemperature(builtInHeat.get().welding())
                        .save();
            }
            var headSupplier = SDSkilletPartItems.HEADS.get(material);
            if(headSupplier != null) {
                var head = headSupplier.get();
                provider.newEntry(BuiltInRegistries.ITEM.getKey(head))
                        .ingredient(head)
                        .heatCapacity(heat200)
                        .forgingTemperature(builtInHeat.get().forging())
                        .weldingTemperature(builtInHeat.get().welding())
                        .save();
            }
        }
        SDSkilletPartItems.LININGS.forEach((metal, item) ->{
            var id = ResourceLocation.fromNamespaceAndPath("tfc", "tfc/item_heat/" + metal.getSerializedName() + "/ingot");
            var builtInHeat
                    = BuiltinHeatJson.readBuiltinHeatTemps(
                            SurvivorsDelight.class,
                            id
                    );
            provider.newEntry(BuiltInRegistries.ITEM.getKey(item.get()))
                    .ingredient(item)
                    .heatCapacity(heat250)
                    .forgingTemperature(builtInHeat.get().forging())
                    .weldingTemperature(builtInHeat.get().welding())
                    .save();
        });
    }
}

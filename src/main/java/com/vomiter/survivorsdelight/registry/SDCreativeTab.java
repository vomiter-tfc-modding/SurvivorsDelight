package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.registry.ModCreativeTabs;

import java.util.Arrays;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;

public class SDCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    private static void safeAccept(RegistryObject<Item> ro, CreativeModeTab.Output output){
        if(ro == null) return;
        output.accept(ro.get());
    }

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID + ".main"))
                    .icon(() -> new ItemStack(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER).get()))
                    .displayItems((parameters, output) -> {
                        output.accept(SDSkilletItems.FARMER.get());
                        Arrays.stream(SkilletMaterial.values()).forEach(m -> {
                            safeAccept(SDSkilletPartItems.HEADS.get(m), output);
                            safeAccept(SDSkilletPartItems.UNFINISHED.get(m), output);
                            safeAccept(SDSkilletItems.SKILLETS.get(m), output);
                        });
                        output.accept(SDSkilletPartItems.LINING_SILVER.get());
                        output.accept(SDSkilletPartItems.LINING_TIN.get());
                        SDBlocks.CABINETS.forEach((wood, ro) -> {
                            output.accept(ro.get().asItem());
                        });
                    })
                    .withTabsBefore(ModCreativeTabs.TAB_FARMERS_DELIGHT.getKey())
                    .build()
    );
}

package com.vomiter.survivorsdelight.registry;

import com.vomiter.survivorsdelight.common.device.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.registry.ModCreativeTabs;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Arrays;
import java.util.function.Supplier;

import static com.vomiter.survivorsdelight.SurvivorsDelight.MODID;

public class SDCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    private static void safeAccept(Supplier<? extends Item> sup, CreativeModeTab.Output output){
        if (sup != null) output.accept(sup.get());
    }

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + MODID + ".main"))
                    .icon(() -> new ItemStack(SDSkilletItems.SKILLETS.get(SkilletMaterial.COPPER).get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.STOVE.get());
                        output.accept(ModItems.COOKING_POT.get());
                        output.accept(SDSkilletItems.FARMER.get());

                        Arrays.stream(SkilletMaterial.values()).forEach(m -> {
                            safeAccept(SDSkilletPartItems.HEADS.get(m), output);
                            safeAccept(SDSkilletPartItems.UNFINISHED.get(m), output);
                            safeAccept(SDSkilletItems.SKILLETS.get(m), output);
                        });

                        output.accept(SDSkilletPartItems.LINING_SILVER.get());
                        output.accept(SDSkilletPartItems.LINING_TIN.get());
                        SDBlocks.CABINETS.forEach((wood, holder) -> output.accept(holder.get().asItem()));
                    })
                    .withTabsBefore(BuiltInRegistries.CREATIVE_MODE_TAB.getKey(ModCreativeTabs.TAB_FARMERS_DELIGHT.get()))
                    .build()
    );
}

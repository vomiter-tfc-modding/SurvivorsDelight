package com.vomiter.survivorsdelight.data.recipe;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletPartItems;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.TerraFirmaCraft;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.ingredients.AndIngredient;
import net.dries007.tfc.common.recipes.ingredients.NotRottenIngredient;
import net.dries007.tfc.common.recipes.outputs.CopyFoodModifier;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.dries007.tfc.util.Metal;
import net.dries007.tfc.util.data.FluidHeat;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.fluids.FluidStack;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SDHeatingRecipes{
    private final ExistingFileHelper existingFileHelper;
    private final Map<Metal, Float> meltTemperatureCache = new EnumMap<>(Metal.class);



    public SDHeatingRecipes(ExistingFileHelper exh) {
        existingFileHelper = exh;
    }

    public void save(RecipeOutput out){
        linings(out);
        skillets(out);
        foods(out);
    }

    public void linings(RecipeOutput out){
        SDSkilletPartItems.LININGS.forEach((m, l) -> {
            out.accept(
                    SDUtils.RLUtils.build("heating/skillet_lining/" + m.getSerializedName()),
                    new HeatingRecipe(
                            Ingredient.of(l),
                            ItemStackProvider.empty(),
                            new FluidStack(meltFluidFor(m), 25),
                            temperatureOf(m),
                            false
                    ),
                    null
            );

        });
    }

    public void skillets(RecipeOutput out){
        out.accept(
                SDUtils.RLUtils.build("heating/skillet/farmer"),
                new HeatingRecipe(
                        Ingredient.of(SDSkilletItems.FARMER.get()),
                        ItemStackProvider.empty(),
                        new FluidStack(meltFluidFor(Metal.STEEL), 450),
                        temperatureOf(Metal.STEEL),
                        false
                ),
                null
        );

        SDSkilletItems.SKILLETS.forEach((m, s) -> {
            Metal defaultMetal = m.getDefault();
            if(defaultMetal == null) return;
            out.accept(
                    SDUtils.RLUtils.build("heating/skillet/" + m.material),
                    new HeatingRecipe(
                            Ingredient.of(s.get()),
                            ItemStackProvider.empty(),
                            new FluidStack(meltFluidFor(defaultMetal), 450),
                            temperatureOf(defaultMetal),
                            false
                    ),
                    null
            );

            Map.of(
                    "skillet_head", SDSkilletPartItems.HEADS,
                    "unfinished_skillet",SDSkilletPartItems.UNFINISHED
                ).forEach((name, partMap) -> {
                var part = partMap.get(m);
                if(part != null) out.accept(
                        SDUtils.RLUtils.build("heating/" + name + "/" + defaultMetal.getSerializedName()),
                        new HeatingRecipe(
                                Ingredient.of(part),
                                ItemStackProvider.empty(),
                                new FluidStack(meltFluidFor(defaultMetal), (name.equals("unfinished_skillet")? 450: 400)),
                                temperatureOf(defaultMetal),
                                false
                        ),
                        null
                );

            });
        });
    }

    float temperatureOf(Metal metal) {
        return meltTemperatureCache.computeIfAbsent(metal, this::readMeltTemperatureFromJson);
    }

    private float readMeltTemperatureFromJson(Metal metal) {
        final String cpPath = "data/" + TerraFirmaCraft.MOD_ID + "/tfc/fluid_heat/" + metal.getSerializedName() + ".json";
        InputStream in = getClass().getClassLoader().getResourceAsStream(cpPath);
        Reader r = new InputStreamReader(in, StandardCharsets.UTF_8);
        FluidHeat fh = FluidHeat.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(r))
                .resultOrPartial(msg -> {
                    System.err.println("[FluidHeatData] parse error: " + msg);
                }).orElse(new FluidHeat(Fluids.EMPTY));
        meltTemperatureCache.put(metal, fh.meltTemperature());
        return fh.meltTemperature();
    }

    Fluid fluidOf(Metal metal) {
        return TFCFluids.METALS.get(metal).getSource();
    }

    private Fluid meltFluidFor(Metal metal)
    {
        return fluidOf(switch (metal)
        {
            case WROUGHT_IRON -> Metal.CAST_IRON;
            case HIGH_CARBON_STEEL -> Metal.PIG_IRON;
            case HIGH_CARBON_BLACK_STEEL -> Metal.WEAK_STEEL;
            case HIGH_CARBON_BLUE_STEEL -> Metal.WEAK_BLUE_STEEL;
            case HIGH_CARBON_RED_STEEL -> Metal.WEAK_RED_STEEL;
            default -> metal;
        });
    }


    public void foods(RecipeOutput out) {
        Map<Item, Item> rawToCooked = new LinkedHashMap<>();
        rawToCooked.put(ModItems.CHICKEN_CUTS.get(),         ModItems.COOKED_CHICKEN_CUTS.get());
        rawToCooked.put(ModItems.BACON.get(),                 ModItems.COOKED_BACON.get());
        rawToCooked.put(ModItems.COD_SLICE.get(),            ModItems.COOKED_COD_SLICE.get());
        rawToCooked.put(ModItems.SALMON_SLICE.get(),         ModItems.COOKED_SALMON_SLICE.get());
        rawToCooked.put(ModItems.MUTTON_CHOPS.get(),         ModItems.COOKED_MUTTON_CHOPS.get());
        rawToCooked.put(ModItems.MINCED_BEEF.get(),          ModItems.BEEF_PATTY.get());
        rawToCooked.put(ModItems.HAM.get(),                  ModItems.SMOKED_HAM.get());

        for (Map.Entry<Item, Item> e : rawToCooked.entrySet()) {
            Item raw = e.getKey();
            Item cooked = e.getValue();

            ResourceLocation rawId = BuiltInRegistries.ITEM.getKey(raw);
            ResourceLocation cookedId = BuiltInRegistries.ITEM.getKey(cooked);

            if (rawId == null || cookedId == null) continue;

            String path = "heating/farmersdelight/" + rawId.getPath(); // e.g. heating/farmersdelight/chicken_cuts.json
            out.accept(
                    SDUtils.RLUtils.build(path),
                    new HeatingRecipe(
                            AndIngredient.of(Ingredient.of(raw), NotRottenIngredient.INSTANCE),
                            ItemStackProvider.of(cooked.getDefaultInstance(), CopyFoodModifier.INSTANCE),
                            FluidStack.EMPTY,
                            200,
                            false
                    ),
                    null
            );
        }
    }
}

package com.vomiter.survivorsdelight.mixin.recipe;

import com.google.gson.JsonElement;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.data.recipe.FDRecipeBlocker;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManager_FDFilterMixin {

    // 原本是 private final，要標 @Mutable 才能重新指定
    @Shadow
    @Mutable
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Shadow
    @Mutable
    private Map<ResourceLocation, Recipe<?>> byName;


    @Inject(method = "apply*", at = @At("TAIL"))
    private void sd$filterFdFoodRecipes(Map<ResourceLocation, JsonElement> json,
                                        ResourceManager resourceManager,
                                        ProfilerFiller profiler,
                                        CallbackInfo ci) {

        //SurvivorsDelight.LOGGER.info("Recipe Filter Operated");
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        RegistryAccess access = server != null ? server.registryAccess() : RegistryAccess.EMPTY;

        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> newRecipes = new HashMap<>();
        Map<ResourceLocation, Recipe<?>> newByName = new HashMap<>(this.byName);

        for (Map.Entry<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> entry : this.recipes.entrySet()) {
            RecipeType<?> type = entry.getKey();
            Map<ResourceLocation, Recipe<?>> originalById = entry.getValue();

            Map<ResourceLocation, Recipe<?>> filteredById = new HashMap<>(originalById);

            if (type == RecipeType.CRAFTING
                    || type == ModRecipeTypes.CUTTING.get()
                    || type == ModRecipeTypes.COOKING.get()) {

                Iterator<Map.Entry<ResourceLocation, Recipe<?>>> it = filteredById.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<ResourceLocation, Recipe<?>> e = it.next();
                    ResourceLocation id = e.getKey();
                    Recipe<?> recipe = e.getValue();

                    if (FDRecipeBlocker.shouldBlock(id, recipe, access)) {
                        //SurvivorsDelight.LOGGER.info("Filtered: {}", id);
                        it.remove();
                        newByName.remove(id);
                    }
                }
            }

            newRecipes.put(type, filteredById);
        }

        this.recipes = Map.copyOf(newRecipes);
        this.byName = Map.copyOf(newByName);
    }
}

package com.vomiter.survivorsdelight.mixin.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.vomiter.survivorsdelight.data.recipe.FDRecipeBlocker;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import java.util.Map;

@Mixin(RecipeManager.class)
public abstract class RecipeManager_FDFilterMixin {

    // 新結構：Multimap<RecipeType<?>, RecipeHolder<?>>
    @Shadow @Mutable
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    // 新結構：Map<ResourceLocation, RecipeHolder<?>>
    @Shadow @Mutable
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    // 直接取用 RecipeManager 內建的 registries（HolderLookup.Provider）
    @Shadow
    private HolderLookup.Provider registries;

    /**
     * 在新版 apply 完成（成功解出 WithConditions/RecipeHolder 後）做過濾再回填。
     * 方法簽章為 apply(Map<ResourceLocation, JsonElement>, ResourceManager, ProfilerFiller)
     */
    @Inject(method = "apply", at = @At("TAIL"))
    private void sd$filterFdFoodRecipes(Map<ResourceLocation, ?> json,
                                        ResourceManager resourceManager,
                                        ProfilerFiller profiler,
                                        CallbackInfo ci) {

        // 重新建構不可變結構的 builder
        ImmutableMultimap.Builder<RecipeType<?>, RecipeHolder<?>> typeBuilder = ImmutableMultimap.builder();
        ImmutableMap.Builder<ResourceLocation, RecipeHolder<?>> nameBuilder = ImmutableMap.builder();

        // 走現有載入結果（注意：此時條件已由 Conditional CODEC 處理完）
        for (RecipeHolder<?> holder : this.byName.values()) {
            final ResourceLocation id = holder.id();
            final Recipe<?> recipe = holder.value();
            final RecipeType<?> type = recipe.getType();

            boolean isTargetType =
                    type == RecipeType.CRAFTING
                            || type == ModRecipeTypes.CUTTING.get()
                            || type == ModRecipeTypes.COOKING.get();

            // 你原本的過濾邏輯：可改成吃（id, recipe, registries）
            boolean block = isTargetType && FDRecipeBlocker.shouldBlock(id, recipe, this.registries);

            if (!block) {
                typeBuilder.put(type, holder);
                nameBuilder.put(id, holder);
            }
        }

        // 回寫覆蓋（維持不可變語意）
        this.byType = typeBuilder.build();
        this.byName = nameBuilder.build();
    }
}

package com.vomiter.survivorsdelight.client.model;

import net.dries007.tfc.common.component.TFCComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Per-bake bindings retain their own rules and models throughout the next resource reload. */
public final class DynamicMealModel extends BakedModelWrapper<BakedModel> {
    private final ItemOverrides overrides;

    public DynamicMealModel(BakedModel original, List<Binding> bindings) {
        super(original);
        List<Binding> snapshot = List.copyOf(bindings);
        overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(@NotNull BakedModel model, @NotNull ItemStack stack, @Nullable ClientLevel level,
                                      @Nullable LivingEntity entity, int seed) {
                var ingredients = stack.get(TFCComponents.INGREDIENTS.get());
                if (ingredients != null) {
                    for (Binding binding : snapshot) {
                        if (binding.rule().matches(stack, ingredients.contents())) {
                            return resolveNested(binding.model(), stack, level, entity, seed);
                        }
                    }
                }
                return resolveNested(original, stack, level, entity, seed);
            }
        };
    }

    private static BakedModel resolveNested(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                           @Nullable LivingEntity entity, int seed) {
        BakedModel resolved = model.getOverrides().resolve(model, stack, level, entity, seed);
        return resolved == null ? model : resolved;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() { return overrides; }

    public record Binding(MealModelRule rule, BakedModel model) {}
}

package com.vomiter.survivorsdelight.common.food;

import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Thread-scoped context for one completeUsingItem call. */
public final class FoodUseContext implements AutoCloseable {
    private static final ThreadLocal<FoodUseContext> CURRENT = new ThreadLocal<>();

    private final FoodUseContext previous;
    private final LivingEntity entity;
    private final ItemStack food;
    private final boolean rotten;
    private boolean nutritionApplied;

    private FoodUseContext(LivingEntity entity, ItemStack food) {
        this.previous = CURRENT.get();
        this.entity = entity;
        this.food = food.copy();
        this.rotten = FoodCapability.isRotten(this.food);
    }

    public static FoodUseContext begin(LivingEntity entity, ItemStack food) {
        FoodUseContext context = new FoodUseContext(entity, food);
        CURRENT.set(context);
        return context;
    }

    public static void markNutritionApplied(LivingEntity entity) {
        FoodUseContext context = CURRENT.get();
        if (context != null && context.entity == entity) context.nutritionApplied = true;
    }

    public static boolean isRottenFood(LivingEntity entity) {
        FoodUseContext context = CURRENT.get();
        return context != null && context.entity == entity && context.rotten;
    }

    public ItemStack food() {
        return food;
    }

    public boolean nutritionApplied() {
        return nutritionApplied;
    }

    @Override
    public void close() {
        if (CURRENT.get() != this) {
            throw new IllegalStateException("Food use contexts must close on their owning thread in reverse order");
        }
        if (previous == null) CURRENT.remove();
        else CURRENT.set(previous);
    }
}

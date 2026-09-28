package com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/**
 * Context passed to dynamic cooking rules. In INDIVIDUAL, stack is one input
 * slot; in TOTAL, stack is the recipe output. Treat lists, stacks and fluids as
 * read-only. The lists cannot be structurally modified through this context.
 * Each nonempty input stack has count one and the fluid amount
 * is the recipe's required amount.
 */
public record DynamicFoodContext<R>(
        List<ItemStack> inputs,
        List<FluidStack> inputFluids,
        ItemStack stack, //can be the input or the result, depending on the phase
        R recipe,
        ResourceLocation recipeId,
        Phase phase,
        Level level
) {
    public DynamicFoodContext {
        inputs = List.copyOf(inputs);
        inputFluids = List.copyOf(inputFluids);
    }

    public DynamicFoodContext(
            List<ItemStack> inputs,
            FluidStack inputFluid,
            ItemStack stack, //can be the input or the result, depending on the phase
            R recipe,
            ResourceLocation recipeId,
            Phase phase,
            Level level
    ){
        this(
                inputs,
                List.of(inputFluid),
                stack,
                recipe,
                recipeId,
                phase,
                level
        );

    }

    public enum Phase {
        INDIVIDUAL,
        TOTAL
    }
}

# Dynamic cooking rules API

For Minecraft 1.20.1 / Forge. The public entry point is
`com.vomiter.survivorsdelight.api.cooking.DynamicCookingRules`.
It extends the existing cooking-pot nutrition calculation.

## Register a rule

Register on both physical sides from `FMLCommonSetupEvent.enqueueWork`.
This example adds a vegetable bonus for each brown mushroom input:

```java
import com.vomiter.survivorsdelight.adapter.cooking_pot.dynamic.DynamicFoodContext;
import com.vomiter.survivorsdelight.api.cooking.DynamicCookingRules;
import net.dries007.tfc.common.capabilities.food.Nutrient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = "exampleaddon", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ExampleCookingRules {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> DynamicCookingRules.register(
                new ResourceLocation("exampleaddon", "mushroom_bonus"),
                DynamicCookingRules.PRIORITY_ADDITION,
                (food, context) -> {
                    if (context.phase() != DynamicFoodContext.Phase.INDIVIDUAL) return food;
                    if (!context.stack().is(Items.BROWN_MUSHROOM)) return food;
                    return food.addNutrient(Nutrient.VEGETABLES, 0.1f);
                }
        ));
    }
}
```

Use your addon's namespace for IDs. A duplicate ID throws an
`IllegalArgumentException` and leaves the existing rule intact. Registration
after load complete throws an `IllegalStateException`. Rules cannot be removed
or replaced at runtime.

## Ordering and phases

| Suggested priority | Constant | Built-in use |
| ---: | --- | --- |
| 2000 | `PRIORITY_ADDITION` | Heated ingredients, bones, pasta/grains |
| 1000 | `PRIORITY_MULTIPLICATION` | Sweetener and salt |
| 0 | `PRIORITY_FLUID` | Oil and milk |

Any integer priority is allowed. Higher values run first. Equal priorities retain
registration order. Use distinct priorities when your rule must run before or after 
another mod's rule. For example, 1500 runs after the built-in addition rules and
before their multiplication rules.

For **each rule**, SD calls `INDIVIDUAL` once for every input slot, then `TOTAL`
once for the output. Only then does it move to the next rule. Empty input slots
are included; use an item/tag check or `isEmpty()` as appropriate. A rule that
should run once per batch must explicitly check for `TOTAL`.

All calls accumulate into one builder. `INDIVIDUAL` does not receive a separate
builder for each ingredient: multiplying it also changes nutrients accumulated
from earlier calls.

| Context accessor | Meaning in the built-in cooking pot |
| --- | --- |
| `inputs()` | Input-slot snapshots; each nonempty stack has count 1 |
| `inputFluids()` | Fluid snapshot, with the recipe's required amount; may contain an empty stack |
| `stack()` | Current input slot in `INDIVIDUAL`; output in `TOTAL` |
| `recipe()` / `recipeId()` | The current `CookingPotRecipe` and its ID |
| `phase()` | `INDIVIDUAL` or `TOTAL` |
| `level()` | Level used by this calculation |

Context lists are unmodifiable snapshots. The stacks and fluids inside them are
mutable Java objects, but callbacks must treat them as read-only. Copy them if
you need to experiment with their data. Do not consume ingredients, change the
output stack, modify the world, or retain per-calculation state in static fields.

## Return the accumulated builder

A modifier must return a non-null `FoodDataBuilder`. It can modify and return the
supplied builder or return a new one. Each return value becomes the input to the
next callback, and the last return value is used by the pot.

For example, a callback may copy the current data before changing it:

```java
return FoodDataBuilder.from(food.build()).addWater(5);
```

Import `com.vomiter.survivorsdelight.util.FoodDataBuilder` for this form. A null
return fails with an error naming the rule and phase. Returning a fresh empty
builder intentionally discards all data accumulated so far.

## What the pot does after rules

Rules execute only when the output has `FoodHandler.Dynamic` 
(this will change in 1.21 as everything is dynamic food in 1.21).
Static food outputs skip this path.

After rules run, the existing pot calculation:

1. Multiplies all five nutrient values by its recipe balance multiplier.
2. Sets the decay modifier to `4.5`.
3. Replaces hunger with `(highest input food hunger + 5) / 2`, using integer division.
4. Saves the final food data, ingredient list and creation date.

Consequently, a rule's nutrient changes still pass through balancing. Hunger and
decay changes made by a rule are overwritten; water and saturation are retained.
`TOTAL` means the output phase of a rule, not a hook after this final balancing.

## Inspect and verify

After load complete, `DynamicCookingRules.getRules()` returns an immutable list
in execution order. `DynamicCookingRules.apply(food, totalContext)` evaluates a
complete rule pass and returns the accumulated builder; it does not perform the
pot's final balancing or save food state. SD owns the internal `freeze()` call.

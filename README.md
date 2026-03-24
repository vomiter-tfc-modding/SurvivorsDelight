# Survivor’s Delight — TFC × FD Compatibility

Make Farmer’s Delight behave correctly in TerraFirmaCraft worlds.

Survivor’s Delight fixes core mismatches between Farmer’s Delight and TFC systems. This mod is not about adding more food content — it restores mechanical consistency so FD workflows respect TFC rules and progression.

***

## Why Survivor’s Delight Exists

Farmer’s Delight is designed for vanilla Minecraft, but several of its assumptions do not hold in TerraFirmaCraft:

*   Cooking tools do not degrade with use
*   Cooking outputs ignore TFC dynamic nutrition
*   Containers bypass TFC material progression
*   Rich soil breaks TFC pacing
*   Food blocks (e. g. Pie and Feast) do not rot in TFC world

Survivor’s Delight resolves these issues and makes FD systems obey TFC mechanics. It is designed to be used alongside content-focused compatibility mods.

***

## Field Guide

*   All features are documented in the in-game book.
*   You may check it out the same way you would for other TFC contents.

***

## Cabinets (TFC-native Storage)

*   Each TFC wood type has its own cabinet variant.
*   Cabinets can be treated with a wooden tallow bucket or beeswax (from Firmalife).
*   Treated cabinets preserve food for a longer period.
*   Storage aesthetics and progression now align with TFC materials.

***

## Cooking Pot (Dynamic Food & Containers)

*   Cooking pot now holds fluid.
*   Water can be used to cook TFC soup and boiled eggs.
*   Several FD dishes are adjusted to require fluid in their recipes.
*   Cooking pot calculates nutrient values for output items when the result is a dynamic food.
*   Cooking pot accepts TFC glass bottles and ceramic bowls as valid containers.
*   Output dishes return the correct container after being consumed or used in crafting.
*   Stored output can rot if left unattended, just like normal foods.
*   Rotten stored stacks can be cleared using a wooden water bucket.
*   JEI display is adjusted for recipes requiring fluid.

***

## Skillet (Heat & Tool Degradation)

*   Skillet blocks read real heat from below.
*   Cooking is based on the actual temperature of the TFC heat source (e.g., firepit).
*   When the target temperature is reached, the dish is produced.
*   Skillet prioritizes TFC Heating logic and applies the `SKILLET_COOKED` trait.
*   Instead of cooking ingredients one by one, the skillet heats the entire stack at once.
*   When used in hand, only one item can be heated at a time.
*   Input slot cap is limited (default: 8) to better fit TFC pacing.
*   Skillets have different TFC metal variants.
*   Cooking with a skillet consumes durability.
*   The skillet will not break (if you don't use it in a combat) and can be reforged later.

***

## Stove (Fuel & Heat Integration)

*   Stove now requires fuel to operate.
*   Fuel amount is displayed when holding fuel and looking at the stove.
*   Stove consumes fuel at a very slow rate when idle.
*   Fuel consumption increases when actively cooking.
*   Stove can heat oven tops, pots, and vats if Firmalife is installed.

***

## Cutting Board (Food State Preservation)

*   Cutting board recipes respect TFC item providers.
*   Food states (e.g., salted) are preserved when using `"tfc:copy_food"` modifiers.
*   Furniture salvaging recipes are provided for TFC wood types.

***

## Food Behavior Adjustments

*   Feast and Pie blocks now use block entities to track food properties.
*   Rotten food blocks visually turn green when spoiled.
*   Rotten foods no longer provide beneficial mob effects.
*   Nourishment now properly negates food exhaustion under TFC’s food system.
*   Dog food grants extra familiarity to wolves.
*   Buffs apply only when familiarity is not below the adult cap (default: 35).
*   Horse feed provides buffs to sufficiently familiar horses.
*   Buffed horses can carry heavier items without becoming overburdened.
*   Certain sandwich types use Farmer’s Delight sandwich textures.
*   Rotten TFC tomatoes can be thrown as FD rotten tomatoes.

***

## Ham (Realistic Acquisition)

*   Ham can be obtained by killing suidae or pork-dropping animals using piercing damage (javelins, knives, arrows).
*   Drop rate scales with animal familiarity (if applicable).
*   Each animal can drop up to 2 hams.

***

## Rich Soil (Balanced Progression)

*   Growth boost is adjusted to fit TFC pacing.
*   Rich soil can reduce sapling wait time, but no longer causes rapid full growth.
*   Rich soil can randomly generate mushrooms on top of itself.
*   Rich soil farmland supports TFC crop growth.
*   Climate range for crops planted on rich soil farmland is expanded (configurable).

***

## Requirements

*   Minecraft / Forge
*   TerraFirmaCraft
*   Farmer’s Delight
*   Survivor’s Abilities (>= 1.2.0)
*   Firmalife (optional, for chocolate-related recipes and minor features)

***

## Compatibility

*   **FarmersDelightTFC**

    *   Most food values and recipes are overridden by Survivor’s Delight.
    *   Tags provided by FDTFC still work as usual.
    *   Most non-food recipes remain compatible.
*   **Rosia**

    *   Heat sources can heat skillets and stoves.
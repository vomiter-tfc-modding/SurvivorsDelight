# 2026/01/
## 23rd
- Refactor: Cabinet

# 2026/2/18
## Config
- Added three new Common config options:
    - `richSoilFarmlandAllowNonTFCCrop`: If disabled, non-TFC crops cannot be grown on FD rich soil farmland. Enabled by default.
    - `richSoilRandomMushroomChance`: Controls the probability (0–1) that Rich Soil spawns a mushroom above it during a random tick. 1 by default.
    - `richSoilRandomMushroomBrownChance`: When a mushroom is spawned, controls the probability (0–1) that it is a Brown Mushroom (otherwise Red Mushroom). 1/10 by default.
- All Rich Soil–related config values are cached into runtime static fields to avoid repeated config spec lookups during gameplay.

## Gameplay / Farming
- Rich Soil random tick behavior now supports configurable mushroom spawning:
    - Spawn probability and mushroom type are driven by config rather than hardcoded values.
- Non-TFC crop gating on Rich Soil Farmland:
  - When non-TFC crops are disallowed, blocks above Rich Soil Farmland that lack a `CropBlockEntity`, are not TFC crops, and are not dead TFC crops will be removed after the delayed check.
  - Crop validation above Rich Soil Farmland is deferred by 5 ticks after player interaction.
  - This prevents valid crops from being removed before their `BlockEntity` is fully initialized.

## Recipe Rebalance
- Rebalance exp value of most cooking recipes.

## ATM Compatibility (AlmostUnified)
- Improved integration with the recipe processing pipeline used in ATM modpacks, where recipes are unified and transformed by **AlmostUnified** before final loading.
- The filtering step has been repositioned to run after AlmostUnified’s transformation stage, ensuring consistent behavior within ATM environments.
- Verified to work correctly in ATM packs using **AlmostUnified 0.9.4** as well as the latest released version **0.11.0**.
- This change only affects environments where AlmostUnified is present. In setups without it, the standard recipe loading behavior remains unchanged.
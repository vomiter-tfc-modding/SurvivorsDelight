package com.vomiter.survivorsdelight.mixin.device.skillet;

import com.vomiter.survivorsdelight.adapter.skillet.skillet_block.SkilletBlockCookingAdapter;
import com.vomiter.survivorsdelight.util.HeatHelper;
import net.dries007.tfc.common.capabilities.heat.HeatCapability;
import net.dries007.tfc.common.capabilities.heat.IHeat;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.entity.HeatableBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.mixin.accessor.RecipeManagerAccessor;
import vectorwing.farmersdelight.common.registry.ModSounds;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import java.util.Optional;

@Mixin(value = SkilletBlockEntity.class, remap = false)
public abstract class SkilletBlockEntity_TFCHeatMixin extends BlockEntity implements HeatableBlockEntity {

    @Final @Shadow private ItemStackHandler inventory;
    @Shadow private int cookingTime;
    @Shadow private ResourceLocation lastRecipeID;

    @Shadow private ItemStack skilletStack;

    SkilletBlockEntity_TFCHeatMixin(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    @Shadow public abstract void setSkilletItem(ItemStack stack);

    @Shadow
    public abstract boolean hasStoredStack();

    @Shadow
    public abstract ItemStack getStoredStack();

    // tfc cached recipe
    @Unique private HeatingRecipe sdtfc$cachedHeatingRecipe = null;

    @Inject(method = "addItemToCook", at = @At("HEAD"), cancellable = true)
    private void sdtfc$acceptHeatingRecipeOnAdd(ItemStack addedStack, Player player, CallbackInfoReturnable<ItemStack> cir) {
        final SkilletBlockEntity self = (SkilletBlockEntity) (Object) this;
        final Level lvl = self.getLevel();
        if (lvl == null || addedStack.isEmpty() || hasStoredStack()) return;

        var heating = SkilletBlockCookingAdapter.getHeatingRecipe(addedStack);
        if(heating == null) return;

        // Mimicking original behavior
        // 1) not allowed if Waterlogged
        if(SkilletBlockCookingAdapter.checkWaterLogged(self, player)) cir.setReturnValue(addedStack);

        // 2) set HeatingRecipe cache
        this.sdtfc$cachedHeatingRecipe = heating;

        // 3) insert
        boolean wasEmpty = inventory.getStackInSlot(0).isEmpty();
        ItemStack remainder = self.getInventory().insertItem(0, addedStack.copy(), false);
        boolean insertionSuccess = !ItemStack.matches(remainder, addedStack);
        if (insertionSuccess) {
            this.lastRecipeID = null;
            this.cookingTime = 0;

            //play sound
            final BlockPos pos = self.getBlockPos();
            if (!ItemUtils.isInventoryEmpty(inventory) && wasEmpty) {
                lvl.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                        ModSounds.BLOCK_SKILLET_ADD_FOOD.get(),
                        SoundSource.BLOCKS, 0.8F, 1.0F);
            }

            cir.setReturnValue(remainder);
            return;
        }

        cir.setReturnValue(addedStack);
    }


    /**
     If heating recipe exist -> add heat until the temperature is reached and done
     */
    @Inject(method = "cookAndOutputItems", at = @At("HEAD"), cancellable = true)
    private void sdtfc$cookWithBelowTemperature(ItemStack cookingStack, Level level, CallbackInfo ci) {
        final SkilletBlockEntity self = (SkilletBlockEntity) (Object) this;
        final BlockPos pos = self.getBlockPos();

        if (level == null || getStoredStack().isEmpty()) return;
        //if (sdtfc$hasCampfireRecipe(level, getStoredStack())) return;
        SkilletBlockCookingAdapter.noCookForBrokenSkillet(self, skilletStack);

        // get TFC HeatingRecipe
        if (sdtfc$cachedHeatingRecipe == null) {
            sdtfc$cachedHeatingRecipe = HeatingRecipe.getRecipe(new ItemStackInventory(cookingStack));
            if (sdtfc$cachedHeatingRecipe == null) return; //fallback to original
        }

        //check and modify current heat and temperature
        final float belowTemp = sdtfc$getBelowDeviceTemperatureSafe();
        final IHeat heat = HeatCapability.get(getStoredStack());
        if (heat == null || belowTemp <= 0f) {
            ci.cancel();
            return;
        }
        HeatCapability.addTemp(heat, belowTemp);

        //if the recipe temperature is reached (this part is modified from iron grill)
        if (sdtfc$cachedHeatingRecipe.isValidTemperature(heat.getTemperature())) {
            SkilletBlockCookingAdapter.finishCooking(self, sdtfc$cachedHeatingRecipe, skilletStack, belowTemp);
            cookingTime = 0;
            sdtfc$cachedHeatingRecipe = null;
        }

        ci.cancel();
    }

    @Inject(method = "removeItem", at = @At("TAIL"))
    private void sdtfc$clearCacheOnRemove(CallbackInfoReturnable<ItemStack> cir) {
        sdtfc$cachedHeatingRecipe = null;
    }

    /**
     get temperature of the block below the skillet
     */
    @Unique
    private float sdtfc$getBelowDeviceTemperatureSafe() {
        final Level level = getLevel();
        if (level == null) return 0f;
        final BlockPos pos = getBlockPos();
        return HeatHelper.getTargetTemperature(pos, level, requiresDirectHeat(), HeatHelper.GetterType.BLOCK);
    }

    /**
     Check campfire cooking recipe. Basically copied from FD getMatchingRecipe
     */
    @Unique
    private boolean sdtfc$hasCampfireRecipe(Level level, ItemStack stack) {
        var recipeWrapper = new SimpleContainer(new ItemStack[]{stack});
        if (this.lastRecipeID != null) {
            Recipe<Container> recipe = ((RecipeManagerAccessor)level.getRecipeManager()).getRecipeMap(RecipeType.CAMPFIRE_COOKING).get(this.lastRecipeID);
            if (recipe instanceof CampfireCookingRecipe && recipe.matches(recipeWrapper, level)) {
                return true;
            }
        }

        Optional<CampfireCookingRecipe> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CAMPFIRE_COOKING, recipeWrapper, level);
        return recipe.isPresent();
    }
}

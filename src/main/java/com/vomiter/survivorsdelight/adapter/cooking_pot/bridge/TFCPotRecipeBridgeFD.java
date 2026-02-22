package com.vomiter.survivorsdelight.adapter.cooking_pot.bridge;


import com.mojang.authlib.GameProfile;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.blocks.devices.PotBlock;
import net.dries007.tfc.common.recipes.JamPotRecipe;
import net.dries007.tfc.common.recipes.PotRecipe;
import net.dries007.tfc.common.recipes.RecipeHelpers;
import net.dries007.tfc.common.recipes.SoupPotRecipe;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.UUID;

public class TFCPotRecipeBridgeFD extends CookingPotRecipe implements IFluidRequiringRecipe {
    private FluidStackIngredient fluidStackIngredient;

    @Override
    public @Nullable FluidStackIngredient sdtfc$getFluidIngredient() {
        return fluidStackIngredient;
    }

    @Override
    public int sdtfc$getRequiredFluidAmount() {
        return fluidStackIngredient.amount();
    }

    @Override
    public void sdtfc$setFluidRequirement(@Nullable FluidStackIngredient ing, int amount) {
        if(ing == null){
            fluidStackIngredient = FluidStackIngredient.EMPTY;
            return;
        }
        fluidStackIngredient = new FluidStackIngredient(ing.ingredient(), amount);
    }

    public void setFluidStackIngredient(FluidStackIngredient fluidStackIngredient){
        this.fluidStackIngredient = fluidStackIngredient;
    }

    public TFCPotRecipeBridgeFD(ResourceLocation id, NonNullList<Ingredient> inputItems, ItemStack output, ItemStack container, int cookTime) {
        super(id, "tfc_pot_bridge", null, inputItems, output, container, 0, cookTime);
    }

    public static TFCPotRecipeBridgeFD bridge(
            Level level,
            IItemHandler items,
            IFluidHandler fluids
    ){
        int[] arr = {0,1,2,3,4,5};
        PotRecipe tfcPotRecipe = TFCPotRecipeMatcher.findFirstMatch(level, items, fluids, arr).orElse(null);
        if(tfcPotRecipe == null) return null;
        ResourceLocation id = SDUtils.RLUtils.build(SurvivorsDelight.MODID, "cooking_pot/" + tfcPotRecipe.getId().getPath());
        NonNullList<Ingredient> inputItems = NonNullList.create();
        for (int i = 0; i < items.getSlots(); i++) {
            if(i > 5) continue;
            var stack = items.getStackInSlot(i);
            if(!stack.isEmpty()) inputItems.add(Ingredient.of(stack.getItem()));
        }

        var inv = TFCPotInventorySnapshots.snapshot(level, items, fluids);

        ItemStack output = TFCPotRecipeBridgeFD.getOutputAsItemStack(tfcPotRecipe, inv, level).copy();
        ItemStack container = tfcPotRecipe instanceof SoupPotRecipe? Items.BOWL.getDefaultInstance(): ItemStack.EMPTY;
        int cookTime = (int) ((float)tfcPotRecipe.getDuration() / 5f);
        var bridge = new TFCPotRecipeBridgeFD(id, inputItems, output, container, cookTime);
        bridge.setFluidStackIngredient(tfcPotRecipe.getFluidIngredient());
        RecipeHelpers.clearCraftingInput();

        return bridge;
    }

    private static ItemStack getOutputAsItemStack(PotRecipe tfcPotRecipe, PotBlockEntity.PotInventory inv, Level level){
        PotRecipe.Output out = tfcPotRecipe.getOutput(inv);
        if(tfcPotRecipe instanceof SoupPotRecipe){
            if(level.isClientSide) return ItemStack.EMPTY;
            GameProfile profile = new GameProfile(UUID.randomUUID(), "sd-temp-fake");
            FakePlayer fakePlayer = FakePlayerFactory.get((ServerLevel) level, profile);
            fakePlayer.setGameMode(GameType.SURVIVAL);
            var tempPot = new PotBlockEntity(BlockPos.ZERO, PotBlock.stateById(0));
            for (int i = 4; i < 9; i++){
                out.onInteract(tempPot, fakePlayer, Items.BOWL.getDefaultInstance().split(1));
            }
            ItemStack output = fakePlayer.getInventory().items.get(0).copy();
            fakePlayer.remove(Entity.RemovalReason.DISCARDED);
            tempPot.invalidateCapabilities();
            return output;
        }

        if(tfcPotRecipe instanceof JamPotRecipe) return ItemStack.EMPTY;

        RecipeHelpers.setCraftingInput(inv, 4, 9);

        out.onFinish(inv);
        ItemStack output = ItemStack.EMPTY;
        for (int i = 4; i < 9; i++) {
            if(i == 4) {
                output = inv.getStackInSlot(4).copy();
            }
            else{
                if(!inv.getStackInSlot(i).isEmpty()) output.grow(1);
            }
            inv.setStackInSlot(i, ItemStack.EMPTY);
        }
        return output;

    }
}

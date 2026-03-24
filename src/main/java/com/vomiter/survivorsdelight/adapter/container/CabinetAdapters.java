package com.vomiter.survivorsdelight.adapter.container;

import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.blockentities.TFCChestBlockEntity;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.fluids.SimpleFluid;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.common.items.FluidContainerItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import vectorwing.farmersdelight.common.item.SkilletItem;

public class CabinetAdapters {

    private static final DeferredHolder<FoodTrait, FoodTrait> CABINET_STORED = SDFoodTraits.CABINET_STORED;
    public static void setStored(ItemStack food){
        FoodCapability.applyTrait(food, CABINET_STORED);
    }
    public static void removeStored(ItemStack food){
        FoodCapability.removeTrait(food, CABINET_STORED);
    }


    public static boolean isValidItemInCabinet(ItemStack stack){
        return TFCChestBlockEntity.isValid(stack) || stack.getItem() instanceof SkilletItem;
    }

    public static boolean tryTreatWithItem(Player player, InteractionHand hand, ItemStack stack) {

        if (stack.getItem() instanceof FluidContainerItem) {
            IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (handler != null && handler.getTanks() > 0) {
                boolean isTallow = handler.getFluidInTank(0).getFluid()
                        .isSame(TFCFluids.SIMPLE_FLUIDS.get(SimpleFluid.TALLOW).getSource());
                if (isTallow) {
                    handler.drain(100, IFluidHandlerItem.FluidAction.EXECUTE);
                    return true;
                }
            }
        }
        else if (stack.is(SDTags.ItemTags.WOOD_PRESERVATIVES)) {
            if (stack.isDamageableItem()) {
                EquipmentSlot slot = (hand == InteractionHand.MAIN_HAND)
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                stack.hurtAndBreak(1, player, slot);
            } else {
                stack.shrink(1);
            }
            return true;
        }
        return false;
    }

}

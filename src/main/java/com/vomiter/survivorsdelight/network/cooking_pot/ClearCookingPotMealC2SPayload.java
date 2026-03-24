package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.common.device.cooking_pot.ICookingPotCommonMenu;
import com.vomiter.survivorsdelight.legacy.LEGACY_ICookingPotRecipeBridge;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

public record ClearCookingPotMealC2SPayload(BlockPos pos) implements CustomPacketPayload {

    public static final Type<ClearCookingPotMealC2SPayload> TYPE =
            new Type<>(SDUtils.RLUtils.build("clear_pot_meal"));

    public static final StreamCodec<FriendlyByteBuf, ClearCookingPotMealC2SPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, ClearCookingPotMealC2SPayload::pos,
                    ClearCookingPotMealC2SPayload::new
            );

    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }

    private static boolean isAndConsumeWaterBucket(ItemStack stack) {
        // 這裡保留你原本用的 TFC Helpers，內部再嘗試 NeoForge 的新 Capabilities
        IFluidHandler handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null && handler.getTanks() > 0
                && handler.getFluidInTank(0).getFluid().isSame(Fluids.WATER)
                && handler.getFluidInTank(0).getAmount() >= 1000) {
            handler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    public static void handle(ClearCookingPotMealC2SPayload msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var sp = ctx.player();
            if (sp == null) return;

            if (!(sp.containerMenu instanceof ICookingPotCommonMenu commonMenu)) return;
            if (!commonMenu.sdtfc$getBlockEntity().getBlockPos().equals(msg.pos())) return;

            CookingPotBlockEntity pot = commonMenu.sdtfc$getBlockEntity();
            ItemStack meal = pot.getMeal();
            if (meal.isEmpty()) return;

            ItemStack carried = ((net.minecraft.world.inventory.AbstractContainerMenu) commonMenu).getCarried();
            if (!isAndConsumeWaterBucket(carried)) return;

            meal.setCount(0);
            //TODO: use other interface, this one is deprecated
            ((LEGACY_ICookingPotRecipeBridge) pot).sdtfc$setCachedDynamicFoodResult(ItemStack.EMPTY);
            ((LEGACY_ICookingPotRecipeBridge) pot).sdtfc$setCachedBridge(null);
            pot.setChanged();
            ((net.minecraft.world.inventory.AbstractContainerMenu) commonMenu).broadcastChanges();
        });
    }
}

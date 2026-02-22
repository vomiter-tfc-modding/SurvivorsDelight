package com.vomiter.survivorsdelight.network.cooking_pot;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.adapter.cooking_pot.bridge.ICookingPotRecipeBridge;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.ICookingPotCommonMenu;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.network.NetworkEvent;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

import java.util.function.Supplier;

/**
 * Client 想要「用手上的水桶清掉 FD 鍋子的 meal slot」時發這個
 */
public record ClearCookingPotMealC2SPacket(BlockPos pos) {

    public static void encode(ClearCookingPotMealC2SPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.pos());
    }

    public static ClearCookingPotMealC2SPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        return new ClearCookingPotMealC2SPacket(pos);
    }

    private static boolean sdtfc$isWaterBucket(ItemStack stack) {
        IFluidHandler fluidHandler = Helpers.getCapability(stack, Capabilities.FLUID_ITEM);
        if(fluidHandler != null && fluidHandler.getFluidInTank(0).getFluid().isSame(Fluids.WATER)){
            fluidHandler.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }


    public static void handle(ClearCookingPotMealC2SPacket pkt, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer sp = c.getSender();
            if (sp == null) return;

            // 驗證玩家當前開的 menu
            SurvivorsDelight.LOGGER.info("check0");
            if (!(sp.containerMenu instanceof ICookingPotCommonMenu commonMenu)) return;
            AbstractContainerMenu menu = (AbstractContainerMenu) commonMenu;

            // 防偽：封包的 containerId 必須跟目前這個 menu 一致
            SurvivorsDelight.LOGGER.info(commonMenu.sdtfc$getBlockEntity().getBlockPos().toShortString());
            SurvivorsDelight.LOGGER.info(pkt.pos().toShortString());
            SurvivorsDelight.LOGGER.info(pkt.pos.toShortString());
            if (!commonMenu.sdtfc$getBlockEntity().getBlockPos().equals(pkt.pos())) return;

            CookingPotBlockEntity pot = ((ICookingPotCommonMenu)menu).sdtfc$getBlockEntity();
            ItemStack mealStack = pot.getMeal();
            if (mealStack.isEmpty()) {
                return; // 沒東西就不用清
            }

            // 伺服端再驗一次：滑鼠上真的拿的是水桶
            // （menu.getCarried() 是 AbstractContainerMenu 上現在「拿在手上」的 stack）
            var carried = menu.getCarried();
            if (!sdtfc$isWaterBucket(carried)) {
                return;
            }

            // 清掉 meal
            mealStack.setCount(0);
            pot.setChanged();
            ((ICookingPotRecipeBridge)pot).sdtfc$setCachedDynamicFoodResult(ItemStack.EMPTY);
            ((ICookingPotRecipeBridge)pot).sdtfc$setCachedBridge(null);

            // 同步回 client
            menu.broadcastChanges();
        });
        c.setPacketHandled(true);
    }
}

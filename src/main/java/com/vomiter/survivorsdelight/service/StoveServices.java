package com.vomiter.survivorsdelight.service;

import com.vomiter.survivorsdelight.adapter.stove.IStoveBlockEntity;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.util.data.Fuel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class StoveServices {
    public static boolean addFuel(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand) {
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof IStoveBlockEntity stoveEntity) {
            ItemStack heldItem = player.getItemInHand(hand);
            Fuel fuel = Fuel.get(heldItem);
            float logBonus = heldItem.is(TFCTags.Items.FIREPIT_LOGS)? 1.2f: 1;
            if(fuel != null){
                if(stoveEntity.sdtfc$getLeftBurnTick() > IStoveBlockEntity.sdtfc$getMaxDuration()) return false;
                if(!player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
                stoveEntity.sdtfc$addLeftBurnTick(Math.round(fuel.duration() * logBonus * fuel.temperature() * 6 / IStoveBlockEntity.sdtfc$getStaticTemperature()));
                return true;
            }
        }
        return false;
    }

}

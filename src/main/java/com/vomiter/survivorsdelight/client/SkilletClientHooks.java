package com.vomiter.survivorsdelight.client;

import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.network.SDNetwork;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class SkilletClientHooks {

    private static boolean isHoldingAttackableSkillet(Player player) {
        if(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SDSkilletItem sdSkilletItem){
            return sdSkilletItem.canAttack();
        }
        return false;
    }

    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty e) {
        if (isHoldingAttackableSkillet(e.getEntity())) {
            SDNetwork.sendToServer(new SDNetwork.SwingSkilletC2S());
        }
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (isHoldingAttackableSkillet(e.getEntity())) {
            SDNetwork.sendToServer(new SDNetwork.SwingSkilletC2S());
        }
    }
}

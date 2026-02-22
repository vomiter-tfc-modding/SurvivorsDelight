package com.vomiter.survivorsdelight.mixin.compat.firmalife;

import com.vomiter.survivorsdelight.compat.firmalife.StoveTopPotPlace;
import net.dries007.tfc.common.items.TFCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.StoveBlock;

@Mixin(value = StoveBlock.class)
public class StoveBlock_PlacePot {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = true)
    private void place_pot(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if(!stack.is(TFCItems.POT.get())) return;
        if(!ModList.get().isLoaded("firmalife")) return;
        cir.setReturnValue(StoveTopPotPlace.place(state, level, pos, player, hand, hit));
    }

}

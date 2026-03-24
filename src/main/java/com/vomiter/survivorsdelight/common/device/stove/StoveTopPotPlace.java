package com.vomiter.survivorsdelight.common.device.stove;
/*
import com.eerussianguy.firmalife.common.blocks.FLBlocks;
import com.eerussianguy.firmalife.common.util.FLAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class StoveTopPotPlace {
    static public InteractionResult place(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit){
        final BlockPos abovePos = pos.above();
        ItemStack stack = player.getItemInHand(hand);
        if (hit.getDirection() == Direction.UP && level.getBlockState(abovePos).isAir())
        {
            level.setBlockAndUpdate(abovePos, FLBlocks.STOVETOP_POT.get().defaultBlockState());
            if (!player.isCreative()) stack.shrink(1);
            if (player instanceof ServerPlayer server)
            {
                FLAdvancements.STOVETOP_POT.trigger(server);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

 */

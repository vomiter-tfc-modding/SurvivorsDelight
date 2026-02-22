package com.vomiter.survivorsdelight.adapter.food_block;

import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.blockentities.DecayingBlockEntity;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.capabilities.food.IFood;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public interface ISDDecayingBlock {
    int sdtfc$getServingCount(BlockState state);
    default void sdtfc$useGlue(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecayingBlockEntity decaying)) return;
        ItemStack src = decaying.getStack();
        IFood srcFood = FoodCapability.get(src);
        if(srcFood == null) return;

        ItemStack usedItem = player.getItemInHand(hand);
        int servingNumber = sdtfc$getServingCount(state);
        if(srcFood.hasTrait(SDFoodTraits.FOOD_MODEL)){
            cir.setReturnValue(InteractionResult.PASS);
        }
        else if(usedItem.is(SDTags.ItemTags.FOOD_MODEL_COATING) && usedItem.getCount() >= servingNumber){
            srcFood.getTraits().add(SDFoodTraits.FOOD_MODEL);
            usedItem.shrink(servingNumber);
            cir.setReturnValue(InteractionResult.SUCCESS);
            level.sendBlockUpdated(pos, state, state, 3);
        }

    }
}

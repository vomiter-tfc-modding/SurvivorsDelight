package com.vomiter.survivorsdelight.mixin.food.block.basic;

import com.vomiter.survivorsdelight.common.food.block.DecayingPieBlockEntity;
import com.vomiter.survivorsdelight.common.food.block.ISDDecayingBlock;
import com.vomiter.survivorsdelight.common.food.trait.SDFoodTraits;
import com.vomiter.survivorsdelight.registry.SDBlockEntityTypes;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.IFood;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.PieBlock;

@Mixin(PieBlock.class)
public abstract class PieBlock_BlockEntityMixin extends Block implements EntityBlock, ISDDecayingBlock {
    @Shadow @Final public static IntegerProperty BITES;

    @Shadow public abstract int getMaxBites();

    public PieBlock_BlockEntityMixin(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DecayingPieBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return type == SDBlockEntityTypes.PIE_DECAYING.get()
                ? (l, p, st, be) -> DecayingPieBlockEntity.serverTick(l, p, st, (DecayingPieBlockEntity) be)
                : null;
    }

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void sdtfc$use(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<ItemInteractionResult> cir){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecayingPieBlockEntity decayingPieBlockEntity)) return;
        ItemStack src = decayingPieBlockEntity.getStack();
        IFood srcFood = FoodCapability.get(src);
        if(srcFood == null) return;

        ItemStack usedItem = player.getItemInHand(hand);
        int servingNumber = getMaxBites() - state.getValue(BITES);
        if(srcFood.hasTrait(SDFoodTraits.FOOD_MODEL)){
            cir.setReturnValue(ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION);
        }
        else if(usedItem.is(SDTags.ItemTags.FOOD_MODEL_COATING) && usedItem.getCount() >= servingNumber){
            FoodCapability.applyTrait(src, SDFoodTraits.FOOD_MODEL);
            usedItem.shrink(servingNumber);
            cir.setReturnValue(ItemInteractionResult.SUCCESS);
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }


}
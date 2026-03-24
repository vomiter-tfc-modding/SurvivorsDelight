package com.vomiter.survivorsdelight.mixin.food.block.basic;

import com.vomiter.survivorsdelight.common.food.block.DecayingFeastBlockEntity;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.block.FeastBlock;

@Mixin(FeastBlock.class)
public abstract class FeastBlock_BlockEntityMixin extends Block implements EntityBlock, ISDDecayingBlock {
    public FeastBlock_BlockEntityMixin(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new DecayingFeastBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        return type == SDBlockEntityTypes.FEAST_DECAYING.get()
                ? (l, p, st, be) -> DecayingFeastBlockEntity.serverTick(l, p, st, (DecayingFeastBlockEntity) be)
                : null;
    }

    @Shadow public abstract IntegerProperty getServingsProperty();

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void sdtfc$use(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<ItemInteractionResult> cir){
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecayingFeastBlockEntity decayingFeastBlockEntity)) return;
        ItemStack src = decayingFeastBlockEntity.getStack();
        IFood srcFood = FoodCapability.get(src);
        if(srcFood == null) return;

        ItemStack usedItem = player.getItemInHand(hand);
        int servingNumber = state.getValue(getServingsProperty());
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
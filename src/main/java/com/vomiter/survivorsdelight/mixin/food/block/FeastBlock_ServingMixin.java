package com.vomiter.survivorsdelight.mixin.food.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.SDConfig;
import com.vomiter.survivorsdelight.common.food.block.DecayFoodTransfer;
import com.vomiter.survivorsdelight.compat.firmalife.FLCompatHelpers;
import com.vomiter.survivorsdelight.common.food.block.DecayingFeastBlockEntity;
import com.vomiter.survivorsdelight.registry.SDDataComponents;
import com.vomiter.survivorsdelight.registry.component.SDContainer;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.common.component.food.FoodTrait;
import net.dries007.tfc.common.component.food.IFood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import vectorwing.farmersdelight.common.block.FeastBlock;

import java.util.function.Supplier;

@Mixin(value = FeastBlock.class, remap = false)
public abstract class FeastBlock_ServingMixin extends Block {
    @Shadow @Final public Supplier<Item> servingItem;

    @Shadow
    public abstract int getMaxServings();

    public FeastBlock_ServingMixin(Properties p_49795_) {
        super(p_49795_);
    }

    @WrapOperation(
            method = "takeServing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isSameItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z", remap = true)
    )
    private boolean acceptCeramicBowl(ItemStack held, ItemStack compared, Operation<Boolean> original){
        if(compared.is(Items.BOWL) && held.is(TFCBlocks.CERAMIC_BOWL.get().asItem())){
            return true;
        }
        return original.call(held, compared);
    }

    @ModifyVariable(
            method = "takeServing",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lvectorwing/farmersdelight/common/block/FeastBlock;getServingItem(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/item/ItemStack;"
            ),
            name = "serving"
    )
    private ItemStack sdtfc$patchServingAfterBuilt(
            ItemStack serving,
            @Local(argsOnly = true, name = "arg1") LevelAccessor level,
            @Local(argsOnly = true, name = "arg2") BlockPos pos,
            @Local(argsOnly = true, name = "arg3") BlockState state,
            @Local(argsOnly = true, name = "arg4") Player player,
            @Local(argsOnly = true, name = "arg5") InteractionHand hand
    ) {
        if (serving.isEmpty() || serving.getItem() != servingItem.get()) return serving;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof DecayingFeastBlockEntity decayingFeastBlockEntity)) return serving;

        ItemStack src = decayingFeastBlockEntity.getStack();
        float factor;
        if (SDConfig.REBALANCING_FEAST) factor = 1f / (float) getMaxServings();
        else factor = 1f;
        DecayFoodTransfer.copyFoodState(src, serving, true, factor);
        IFood srcFood = FoodCapability.get(src);
        IFood servingFood = FoodCapability.get(serving);

        if (srcFood == null || servingFood == null) return serving;

        FoodCapability.setCreationDate(serving, srcFood.getCreationDate());
        FoodCapability.updateFoodFromPrevious(src, serving);
        if(ModList.get().isLoaded("firmalife")){
            for (Holder<FoodTrait> possibleShelvedFoodTrait : FLCompatHelpers.getPossibleShelvedFoodTraits()) {
                FoodCapability.removeTrait(serving, possibleShelvedFoodTrait);
            }
        }

        if (player.getItemInHand(hand).is(TFCBlocks.CERAMIC_BOWL.get().asItem())) {
            serving.set(SDDataComponents.FOOD_CONTAINER, new SDContainer(TFCBlocks.CERAMIC_BOWL.getId()));
        }
        return serving;
    }}
package com.vomiter.survivorsdelight.adapter.skillet.skillet_block;

import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletUtil;
import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import net.dries007.tfc.common.capabilities.food.FoodCapability;
import net.dries007.tfc.common.recipes.HeatingRecipe;
import net.dries007.tfc.common.recipes.inventory.ItemStackInventory;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import vectorwing.farmersdelight.common.block.SkilletBlock;
import vectorwing.farmersdelight.common.block.entity.SkilletBlockEntity;
import vectorwing.farmersdelight.common.registry.ModSounds;
import vectorwing.farmersdelight.common.utility.ItemUtils;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.Nullable;
import java.util.Objects;

public class SkilletBlockCookingAdapter {

    public static void finishCooking(SkilletBlockEntity skillet, HeatingRecipe recipe, ItemStack skilletStack, float belowTemp){
        assert skillet.getLevel() != null;
        final ItemStack result = recipe.assemble(new ItemStackInventory(skillet.getStoredStack()), skillet.getLevel().registryAccess());

        FoodCapability.applyTrait(result, SkilletUtil.skilletCooked);
        FoodCapability.updateFoodDecayOnCreate(result);

        final BlockState state = skillet.getBlockState();
        var pos = skillet.getBlockPos();

        Direction direction = state.getValue(SkilletBlock.FACING).getClockWise();
        ItemUtils.spawnItemEntity(
                skillet.getLevel(), result.copy(),
                pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                direction.getStepX() * 0.08F, 0.25F, direction.getStepZ() * 0.08F
        );
        skillet.getInventory().extractItem(0, 1, false);
        skillet.getLevel().playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                ModSounds.BLOCK_SKILLET_ADD_FOOD.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
        cookAndHurt(skillet, skilletStack, belowTemp);

    }

    public static void cookAndHurt(SkilletBlockEntity skillet, ItemStack skilletStack, float belowTemp){
        assert skillet.getLevel() != null;
        if(!skillet.getLevel().isClientSide && skilletStack.getItem() instanceof SDSkilletItem){
            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(Objects.requireNonNull(skillet.getLevel().getServer()).getLevel(skillet.getLevel().dimension()));
            fakePlayer.setGameMode(GameType.SURVIVAL);
            skilletStack.hurtAndBreak(1 + SkilletUtil.extraHurtForTemperature(skilletStack, belowTemp), fakePlayer, (user) -> {});
        }
    }

    public static void noCookForBrokenSkillet(SkilletBlockEntity skillet, ItemStack skilletStack){
        if(!(skilletStack.getItem() instanceof  SDSkilletItem sdSkilletItem)) return;
        if(!sdSkilletItem.canCook(skilletStack) && skilletStack.is(SDTags.ItemTags.RETURN_COPPER_SKILLET)){
            CompoundTag tag = skilletStack.serializeNBT();
            tag.putString("id", SkilletMaterial.COPPER.location().toString());
            ItemStack newSkilletStack = ItemStack.of(tag);
            newSkilletStack.setDamageValue(0);
            skillet.setSkilletItem(newSkilletStack);
            assert skillet.getLevel() != null;
            skillet.getLevel().destroyBlock(skillet.getBlockPos(), true);
        }
    }


    public static @Nullable HeatingRecipe getHeatingRecipe(ItemStack addedStack){
        return HeatingRecipe.getRecipe(new ItemStackInventory(addedStack));
    }

    public static boolean checkWaterLogged(BlockEntity skillet, Player player){
        final BlockState state = skillet.getBlockState();
        if (state.hasProperty(SkilletBlock.WATERLOGGED) &&
                state.getValue(SkilletBlock.WATERLOGGED)) {
            player.displayClientMessage(TextUtils.getTranslation("block.skillet.underwater"), true);
            return true;
        }
        return false;
    }

}

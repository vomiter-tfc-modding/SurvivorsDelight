package com.vomiter.survivorsdelight.mixin.device.stove;

import com.vomiter.survivorsdelight.adapter.stove.IStoveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import vectorwing.farmersdelight.common.block.entity.StoveBlockEntity;
import vectorwing.farmersdelight.common.block.entity.SyncedBlockEntity;

@Mixin(value = StoveBlockEntity.class, remap = false)
public abstract class StoveBlockEntity_AccessorImp extends SyncedBlockEntity implements IStoveBlockEntity {
    public StoveBlockEntity_AccessorImp(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public SyncedBlockEntity sdtfc$getBlockEntity(){
        return this;
    };
    public ItemStackHandler sdtfc$getInventory(){
        return ((StoveBlockEntity)(Object)this).getInventory();
    };

    public int[] sdtfc$getCookingTimes(){
        if(this instanceof StoveBlockEntity_Accessor acc){
            return acc.getCookingTimes();
        }
        return null;
    };
    public int[] sdtfc$getCookingTimesTotal(){
        if(this instanceof StoveBlockEntity_Accessor acc){
            return acc.getCookingTimesTotal();
        }
        return null;
    };
    public ResourceLocation[] sdtfc$getLastRecipeIDs(){
        if(this instanceof StoveBlockEntity_Accessor acc){
            return acc.getLastRecipeIDs();
        }
        return null;
    };
}

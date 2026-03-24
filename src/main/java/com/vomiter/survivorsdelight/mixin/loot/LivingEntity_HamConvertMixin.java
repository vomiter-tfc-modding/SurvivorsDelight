package com.vomiter.survivorsdelight.mixin.loot;

import com.llamalad7.mixinextras.sugar.Local;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.TFCTags;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties;
import net.dries007.tfc.common.items.Food;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModItems;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(LivingEntity.class)
public abstract class LivingEntity_HamConvertMixin extends Entity {

    public LivingEntity_HamConvertMixin(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Shadow public abstract long getLootTableSeed();

    @Shadow @Nullable protected Player lastHurtByPlayer;

    @Inject(
            method = "dropFromLootTable(Lnet/minecraft/world/damagesource/DamageSource;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V"
            ),
            cancellable = true
    )
    private void wrapGetRandomItems(
            DamageSource damageSource,
            boolean p_21022_,
            CallbackInfo ci,
            @Local LootTable table,
            @Local LootParams params
    )
    {
        final List<ItemStack> drops = new ArrayList<>();
        var seed = getLootTableSeed();
        table.getRandomItems(params, seed, drops::add);
        boolean hasHam = drops.stream()
                .anyMatch(stack -> stack.is(ModItems.HAM.get())||stack.is(ModItems.SMOKED_HAM.get()));
        if(hasHam) return;
        if(lastHurtByPlayer == null) return;

        TagKey<DamageType> isPiercing = TagKey.create(Registries.DAMAGE_TYPE, SDUtils.RLUtils.build("tfc", "is_piercing"));
        boolean killedWithMeleePiercing = (damageSource.getDirectEntity() instanceof Player killer) && killer.getMainHandItem().is(TFCTags.Items.DEALS_PIERCING_DAMAGE);
        boolean killedWithRangedPiercing = damageSource.is(isPiercing);
        if(!killedWithRangedPiercing && !killedWithMeleePiercing) return;

        int hamToAdd = 0;
        float basicChance = 0.1f;
        var self = (LivingEntity)(Object)this;
        if(self instanceof TFCAnimalProperties animal){
            basicChance += animal.getFamiliarity();
        }
        for (ItemStack stack : drops) {
            if (hamToAdd >= 2) break;
            if (stack.isEmpty() || !stack.is(SDUtils.getTFCFoodItem(Food.PORK))) continue;

            int trials = stack.getCount() / 2;
            for (int i = 0; i < trials; i++) {
                if (getRandom().nextFloat() < basicChance) {
                    stack.shrink(2);
                    hamToAdd += 1;
                    if (stack.getCount() < 2) break;
                }
            }
        }

        if(hamToAdd > 0) drops.add(ModItems.HAM.get().getDefaultInstance().copyWithCount(hamToAdd));
        drops.forEach(this::spawnAtLocation);
        ci.cancel();
    }
}

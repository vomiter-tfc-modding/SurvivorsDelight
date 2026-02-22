package com.vomiter.survivorsdelight.common.device.skillet;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.UUID;

public class SDSkilletItem extends SkilletItem {
    private final Multimap<Attribute, AttributeModifier> toolAttributes;

    public SDSkilletItem(Block block, Properties properties) {
        super(block, properties);
        toolAttributes = null;
    }

    public SDSkilletItem(Block block, Properties properties, Multimap<Attribute, AttributeModifier> toolAttributes) {
        super(block, properties);
        this.toolAttributes = toolAttributes;
    }

    public static UUID getKnockbackUUID(){
        return FD_ATTACK_KNOCKBACK_UUID;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if(!this.canAttack()) return false;
        stack.hurtAndBreak(1, attacker, (user) -> user.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    public boolean canCook(ItemStack stack){
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    public boolean canAttack(){
        return this.toolAttributes != null;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack skilletStack = player.getItemInHand(hand);
        if(skilletStack.is(SDSkilletItems.SKILLETS.get(SkilletMaterial.RED_STEEL).get())||
                skilletStack.is(SDSkilletItems.SKILLETS.get(SkilletMaterial.BLUE_STEEL).get())){
            BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            BlockPos pos = blockhitresult.getBlockPos();
            if(level.getBlockState(pos).is(Blocks.LAVA)){
                skilletStack.enchant(Enchantments.FIRE_ASPECT, 2);
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND && this.canAttack() ? this.toolAttributes : ImmutableMultimap.of();
    }


    public static class SDSkilletEvents {
        public static void playSkilletAttackSound(LivingDamageEvent event) {
            DamageSource damageSource = event.getSource();
            Entity attacker = damageSource.getDirectEntity();
            if (attacker instanceof LivingEntity livingEntity) {
                if (livingEntity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SDSkilletItem) {
                    float pitch = 0.9F + livingEntity.getRandom().nextFloat() * 0.2F;
                    if (livingEntity instanceof Player player) {
                        float attackPower = player.getAttackStrengthScale(0.0F);
                        if (attackPower > 0.8F) {
                            player.getCommandSenderWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_SKILLET_ATTACK_STRONG.get(), SoundSource.PLAYERS, 1.0F, pitch);
                        } else {
                            player.getCommandSenderWorld().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ITEM_SKILLET_ATTACK_WEAK.get(), SoundSource.PLAYERS, 0.8F, 0.9F);
                        }
                    } else {
                        livingEntity.getCommandSenderWorld().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), ModSounds.ITEM_SKILLET_ATTACK_STRONG.get(), SoundSource.PLAYERS, 1.0F, pitch);
                    }
                }
            }
        }
    }

}

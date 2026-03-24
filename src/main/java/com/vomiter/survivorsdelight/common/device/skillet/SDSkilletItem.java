package com.vomiter.survivorsdelight.common.device.skillet;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.registry.ModSounds;

public class SDSkilletItem extends SkilletItem {

    boolean isWeapon;

    public SDSkilletItem(Block block, Properties properties, boolean isWeapon) {
        super(block, properties);
        this.isWeapon = isWeapon;
    }

    public static ResourceLocation getKnockbackUUID(){
        return FD_ATTACK_KNOCKBACK_UUID;
    }

    public static ItemAttributeModifiers sdCreateAttributes(
            Tier tier, float attackDamage, float attackSpeed, float attackKnockBack
    ) {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                                BASE_ATTACK_DAMAGE_ID,
                                attackDamage + tier.getAttackDamageBonus(),
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(
                                BASE_ATTACK_SPEED_ID,
                                (double)attackSpeed - 4,
                                AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                )
                .add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(
                                FD_ATTACK_KNOCKBACK_UUID,
                                attackKnockBack, AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND
                ).build();
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if(!this.canAttack()) return false;
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
        return true;
    }

    public boolean canCook(ItemStack stack){
        return stack.getDamageValue() < stack.getMaxDamage() - 1;
    }

    public boolean canAttack(){
        return isWeapon;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack skilletStack = player.getItemInHand(hand);
        if(skilletStack.is(SDSkilletItems.SKILLETS.get(SkilletMaterial.RED_STEEL).get())||
                skilletStack.is(SDSkilletItems.SKILLETS.get(SkilletMaterial.BLUE_STEEL).get())){
            BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            BlockPos pos = blockhitresult.getBlockPos();
            if(level.getBlockState(pos).is(Blocks.LAVA)){
                skilletStack.enchant(SDUtils.getEnchantHolder(level, Enchantments.FIRE_ASPECT) , 2);
            }
        }
        return super.use(level, player, hand);
    }


    @EventBusSubscriber(
            modid = SurvivorsDelight.MODID
    )
    public static class SDSkilletEvents {
        @SubscribeEvent
        public static void playSkilletAttackSound(LivingDamageEvent.Post event) {
            DamageSource damageSource = event.getEntity().getLastDamageSource();
            if(damageSource == null) return;
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
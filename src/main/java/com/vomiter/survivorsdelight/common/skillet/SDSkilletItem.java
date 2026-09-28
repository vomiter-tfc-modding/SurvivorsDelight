package com.vomiter.survivorsdelight.common.skillet;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.vomiter.survivorsdelight.adapter.skillet.ISkilletItemCookingData;
import com.vomiter.survivorsdelight.data.tags.SDTags;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.client.ClientSetup;
import vectorwing.farmersdelight.client.renderer.SkilletItemRenderer;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.UUID;
import java.util.function.Consumer;

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
        public static void onPlayerTick(TickEvent.PlayerTickEvent event){
            if (!event.phase.equals(TickEvent.Phase.END)) return;
            Player player = event.player;
            if (player instanceof ServerPlayer serverPlayer){
                if (!serverPlayer.isUsingItem() || !serverPlayer.getUseItem().equals(serverPlayer.getMainHandItem())){
                    if (serverPlayer.getMainHandItem().getItem() instanceof SDSkilletItem sdSkilletItem){
                        sdSkilletItem.returnFood(serverPlayer.getMainHandItem(), player);
                    }
                }
            }
        }

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

    void returnFood(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("Cooking")) {
                ItemStack cookingStack = ItemStack.of(tag.getCompound("Cooking"));
                player.getInventory().placeItemBackInInventory(cookingStack);
                tag.remove("Cooking");
                tag.remove("CookTimeHandheld");
            }
            if ((Object)stack instanceof ISkilletItemCookingData data){
                data.clear();
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private static BlockEntityWithoutLevelRenderer renderer = new SkilletItemRenderer();

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity living, InteractionHand hand, ItemStack stack) {
                return stack.getOrCreateTag().contains("FlipTimeStamp") ? ClientSetup.SKILLET_FLIP : null;
            }
        });
    }

    private static int cookingBarWidth = 0;
    public static int getCookingBarWidth() {
        return cookingBarWidth;
    }

    public static void setCookingBarWidth(int cookingBarWidth0) {
        cookingBarWidth = cookingBarWidth0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBarWidth(ItemStack stack) {
        if(stack.getTagElement("Cooking") != null){
            return cookingBarWidth;
        }
        return super.getBarWidth(stack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBarColor(ItemStack stack) {
        if(stack.getTagElement("Cooking") == null) return super.getBarColor(stack);

        return stack.getTagElement("Cooking") != null ? 16747343 : super.getBarColor(stack);
    }

}

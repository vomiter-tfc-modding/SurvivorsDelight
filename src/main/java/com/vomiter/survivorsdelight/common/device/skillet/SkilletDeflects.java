package com.vomiter.survivorsdelight.common.device.skillet;

import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vectorwing.farmersdelight.common.registry.ModSounds;

import java.util.List;

public class SkilletDeflects {

    private static final double SPEED_BOOST = 1.15;
    private static final double HALF_W = 1.5;
    private static final double HALF_H = 1.0;
    private static final double HALF_D = HALF_W;
    private static final double NEAR_Z = 1.5;
    private static final double FAR_Z  = 4.5;
    private static final double COS_LIMIT = Math.cos(Math.toRadians(60));

    public static void performSweepDeflect(Player player) {
        if (player.level().isClientSide) return;
        if (!(player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SDSkilletItem)) return;

        if (player.getAttackStrengthScale(0f) < 0.2f) return;
        ServerLevel level = (ServerLevel) player.level();
        Vec3 eye  = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        AABB boxNear = aabbAt(eye.add(look.scale(NEAR_Z)));
        AABB boxFar  = aabbAt(eye.add(look.scale(FAR_Z)));
        AABB sweep   = new AABB(
                Math.min(boxNear.minX, boxFar.minX), Math.min(boxNear.minY, boxFar.minY), Math.min(boxNear.minZ, boxFar.minZ),
                Math.max(boxNear.maxX, boxFar.maxX), Math.max(boxNear.maxY, boxFar.maxY), Math.max(boxNear.maxZ, boxFar.maxZ)
        );

        List<Projectile> list = level.getEntitiesOfClass(Projectile.class, sweep,
                p -> p.isAlive() && p.distanceToSqr(player) > 0.5);

        if (list.isEmpty()) return;

        for (Projectile proj : list) {
            Vec3 to = proj.position().subtract(eye);
            if (to.lengthSqr() < 1.0e-6) continue;
            Vec3 dirTo = to.normalize();
            if (dirTo.dot(look) < COS_LIMIT) continue;

            double speed = proj.getDeltaMovement().length();
            if (speed < 0.1) speed = 0.6;
            Vec3 newVel = look.scale(speed * SPEED_BOOST);
            Vec3 newPos = eye.add(look.scale(0.6));

            proj.setOwner(player);
            proj.setPos(newPos.x, newPos.y, newPos.z);
            proj.setDeltaMovement(newVel);

            if(player.getItemInHand(InteractionHand.MAIN_HAND).getEnchantmentLevel(SDUtils.getEnchantHolder(level, Enchantments.FIRE_ASPECT)) > 0){
                proj.setRemainingFireTicks(1200);
            }
            proj.hurtMarked = true;
            player.getMainHandItem().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

            if (proj instanceof AbstractArrow arrow) {
                arrow.setCritArrow(true);
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            } else if (proj instanceof ThrownTrident trident) {
                trident.setOwner(player);
                trident.setNoPhysics(false);
            } else if (proj instanceof LargeFireball fireball) {
                fireball.setDeltaMovement(look);
            }
        }

        float pitch = 0.9F + player.getRandom().nextFloat() * 0.2F;
        player.getCommandSenderWorld().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModSounds.ITEM_SKILLET_ATTACK_STRONG.get(),
                SoundSource.PLAYERS,
                1.0F,
                pitch);
    }

    private static AABB aabbAt(Vec3 center) {
        return new AABB(center, center).inflate(HALF_W, HALF_H, HALF_D);
    }
}
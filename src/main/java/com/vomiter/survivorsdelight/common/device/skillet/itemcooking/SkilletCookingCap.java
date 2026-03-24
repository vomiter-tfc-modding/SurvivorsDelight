package com.vomiter.survivorsdelight.common.device.skillet.itemcooking;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.WeakHashMap;

public final class SkilletCookingCap {

    private static final Map<Player, ISkilletItemCookingData> CACHE = new WeakHashMap<>();

    public static final ResourceLocation ID =
            SDUtils.RLUtils.build(SurvivorsDelight.MODID, "skillet_cooking");

    public static final EntityCapability<ISkilletItemCookingData, Void> CAPABILITY =
            EntityCapability.createVoid(ID, ISkilletItemCookingData.class);

    public static void onRegisterCaps(RegisterCapabilitiesEvent event) {
        event.registerEntity(CAPABILITY, EntityType.PLAYER,
                (player, ctx) -> CACHE.computeIfAbsent(player, p -> new SkilletItemCookingData())
        );
    }


    public static void onClone(PlayerEvent.Clone event) {
        Player oldP = event.getOriginal();
        Player newP = event.getEntity();
        var lookup = event.getEntity().level().registryAccess(); // RegistryAccess implements HolderLookup.Provider

        ISkilletItemCookingData oldCap = oldP.getCapability(CAPABILITY);
        ISkilletItemCookingData newCap = newP.getCapability(CAPABILITY);

        if (oldCap != null && newCap != null) {
            ((SkilletItemCookingData) newCap).load(lookup, ((SkilletItemCookingData) oldCap).save(lookup));
        }

        CACHE.remove(oldP);
        CACHE.put(newP, newCap);
    }

    public static ISkilletItemCookingData get(Player player) {
        ISkilletItemCookingData cap = player.getCapability(CAPABILITY);
        if (cap == null) {
            throw new IllegalStateException("SkilletCooking capability missing");
        }
        return cap;
    }
}
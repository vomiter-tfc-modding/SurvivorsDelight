package com.vomiter.survivorsdelight.registry.skillet;

import com.google.common.collect.ImmutableMultimap;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import com.vomiter.survivorsdelight.common.device.skillet.SDSkilletItem;
import com.vomiter.survivorsdelight.adapter.skillet.SkilletMaterial;
import com.vomiter.survivorsdelight.util.ItemUUIDs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class SDSkilletItems {
    static final UUID BASE_ATTACK_DAMAGE_UUID = ItemUUIDs.getBaseAttackDamageUUID();
    static final UUID BASE_ATTACK_SPEED_UUID = ItemUUIDs.getBaseAttackSpeedUUID();
    static final UUID KNOCKBACK_UUID = SDSkilletItem.getKnockbackUUID();

    private SDSkilletItems() {}
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, SurvivorsDelight.MODID);

    public static final Map<SkilletMaterial, RegistryObject<Item>> SKILLETS = new EnumMap<>(SkilletMaterial.class);
    public static RegistryObject<Item> get(SkilletMaterial m){
        return SKILLETS.get(m);
    }
    public static ResourceKey<Item> getKey(SkilletMaterial m){
        return SKILLETS.get(m).getKey();
    }
    public static RegistryObject<Item> FARMER;
    static {
        for (SkilletMaterial m : SkilletMaterial.values()) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            if(m.isWeapon){
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", m.attackDamage, AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.1F, AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(KNOCKBACK_UUID, "Tool modifier", m.attackKnockback, AttributeModifier.Operation.ADDITION));
            }
            Item.Properties properties = new Item.Properties();
            if(m.metal != null){
                properties.rarity(m.metal.getRarity());
            }
            properties.durability(m.durability);
            if(m.equals(SkilletMaterial.RED_STEEL) || m.equals(SkilletMaterial.BLUE_STEEL)){
                properties = properties.fireResistant();
            }
            Item.Properties finalProperties = properties;
            RegistryObject<Item> ro = ITEMS.register(m.path(), () ->
                    m.isWeapon?
                        new SDSkilletItem(SDSkilletBlocks.SKILLETS.get(m).get(), finalProperties, builder.build()):
                        new SDSkilletItem(SDSkilletBlocks.SKILLETS.get(m).get(), finalProperties)
            );
            SKILLETS.put(m, ro);
            if(m.equals(SkilletMaterial.STEEL)){
                FARMER = ITEMS.register("skillet/farmer", () ->
                                new SDSkilletItem(SDSkilletBlocks.FARMER.get(), finalProperties, builder.build())
                );
            }
        }
    }
}

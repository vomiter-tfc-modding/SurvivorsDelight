package com.vomiter.survivorsdelight.compat.jade;

import com.vomiter.survivorsdelight.common.food.block.SDDecayingBlockEntity;
import com.vomiter.survivorsdelight.util.SDUtils;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import vectorwing.farmersdelight.common.block.FeastBlock;
import vectorwing.farmersdelight.common.block.PieBlock;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@WailaPlugin
public class SDJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.registerBlockComponent(FDDecayComponentProvider.INSTANCE, FeastBlock.class);
        reg.registerBlockComponent(FDDecayComponentProvider.INSTANCE, PieBlock.class);
    }

    public enum FDDecayComponentProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig cfg) {
            var blockEntity = accessor.getBlockEntity();
            if (!(blockEntity instanceof SDDecayingBlockEntity decay)) return;

            ItemStack stack = decay.getStack();
            if (stack.isEmpty()) return;

            tooltip.add(stack.getHoverName());
            List<Component> lines = new ArrayList<>();
            FoodCapability.addTooltipInfo(stack, lines::add);
            lines.forEach(tooltip::add);
        }

        @Override
        public ResourceLocation getUid() {
            return SDUtils.RLUtils.build("survivorsdelight", "feast_decay");
        }
    }

}


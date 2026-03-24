package com.vomiter.survivorsdelight.client;

import com.vomiter.survivorsdelight.client.screen.SDCabinetScreen;
import com.vomiter.survivorsdelight.client.screen.SDPotFluidScreen;
import com.vomiter.survivorsdelight.compat.jei.JEIIntegration;
import com.vomiter.survivorsdelight.common.container.SDCabinetBlockEntity;
import com.vomiter.survivorsdelight.common.container.SDCabinetMenu;
import com.vomiter.survivorsdelight.common.device.cooking_pot.fluid_handle.SDCookingPotFluidMenu;
import com.vomiter.survivorsdelight.common.device.stove.IStoveBlockEntity;
import com.vomiter.survivorsdelight.registry.SDContainerTypes;
import com.vomiter.survivorsdelight.registry.skillet.SDSkilletItems;
import net.dries007.tfc.client.ClientHelpers;
import net.dries007.tfc.common.component.food.FoodCapability;
import net.dries007.tfc.util.data.Fuel;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.jetbrains.annotations.NotNull;
import vectorwing.farmersdelight.client.renderer.SkilletItemRenderer;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;
import vectorwing.farmersdelight.common.registry.ModDataComponents;

public class ClientForgeEventHandler {

    public static void init(){
        final IEventBus bus = NeoForge.EVENT_BUS;
        bus.addListener(ClientForgeEventHandler::onItemTooltip);
        bus.addListener(SkilletClientHooks::onLeftClickBlock);
        bus.addListener(SkilletClientHooks::onLeftClickEmpty);
        bus.addListener(ClientForgeEventHandler::onRenderGameOverlayPost);
    }

    private static void drawCenteredText(Minecraft minecraft, GuiGraphics graphics, Component text, int x, int y)
    {
        final int textWidth = minecraft.font.width(text) / 2;
        graphics.drawString(minecraft.font, text, x - textWidth, y, 0xCCCCCC, false);
    }

    public static void registerMenuScreens(RegisterMenuScreensEvent event){
        event.register(SDContainerTypes.CABINET.get(), SDCabinetScreen::new);
        event.register(SDCookingPotFluidMenu.TYPE, SDPotFluidScreen::new);
    }

    private static void cabinetNotTreatedTooltip(ItemTooltipEvent event) {
        final Screen screen = Minecraft.getInstance().screen;
        if (!(screen instanceof SDCabinetScreen cabinetScreen)) return;
        if(!(cabinetScreen.getSlotUnderMouse() instanceof SDCabinetMenu.SDCabinetSlot cabinetSlot)) return;
        if(!(cabinetScreen.getMenu().getContainer() instanceof SDCabinetBlockEntity cabinet)) return;
        if(cabinet.isTreated()) return;
        if(FoodCapability.get(event.getItemStack()) == null) return;
        event.getToolTip().add(Component.translatable("tooltip.survivorsdelight.cabinet_not_treated").withStyle(ChatFormatting.RED));
    }


    public static void onItemTooltip(ItemTooltipEvent event) {
        cabinetNotTreatedTooltip(event);
    }

    static boolean isInJEI(){
        if(ModList.get().isLoaded("jei")) return JEIIntegration.isJEIScreen();
        return false;
    }

    private static void registerSkilletPredicate(Item skillet){
        ItemProperties.register(skillet, ResourceLocation.withDefaultNamespace("cooking"),
                (stack, world, entity, s) -> stack.getOrDefault(ModDataComponents.SKILLET_INGREDIENT, ItemStackWrapper.EMPTY).getStack().isEmpty() ? 0 : 1);
    }

    public static void onClientSetup(FMLClientSetupEvent e){
        SDSkilletItems.SKILLETS.forEach((m, skillet) -> registerSkilletPredicate(skillet.get()));
        registerSkilletPredicate(SDSkilletItems.FARMER.get());
    }


    public static void onRenderGameOverlayPost(RenderGuiLayerEvent.Post event){
        final GuiGraphics stack = event.getGuiGraphics();
        final Minecraft minecraft = Minecraft.getInstance();
        final Player player = minecraft.player;
        if (player != null)
        {
            boolean isHoldingFuel =
                    Fuel.get(player.getMainHandItem()) != null||
                            Fuel.get(player.getOffhandItem()) != null;
            if (
                    event.getName() == VanillaGuiLayers.CROSSHAIR
                            && minecraft.screen == null
                            && isHoldingFuel
                            && (! player.isShiftKeyDown())
            ) {
                final BlockPos targetedPos = ClientHelpers.getTargetedPos();
                if(minecraft.level == null || targetedPos == null) return;
                final BlockEntity targetedBlockEntity = minecraft.level.getBlockEntity(targetedPos);
                if(targetedBlockEntity instanceof IStoveBlockEntity iStove){
                    int x = stack.guiWidth() / 2 + 3;
                    int y = stack.guiHeight() / 2 + 8;
                    Component text = Component.translatable("overlay.survivorsdelight.stove_fuel_amount")
                            .append(Component.literal(": "))
                            .append(Component.literal(String.format(
                                    "%.1f",
                                    Math.min(100, 100f * (float)iStove.sdtfc$getLeftBurnTick() / (float)IStoveBlockEntity.sdtfc$getMaxDuration()))
                            ))
                            .append(Component.literal(" %"));
                    drawCenteredText(minecraft, stack, text, x, y);
                }
            }
        }
    }


    private static IClientItemExtensions buildSkilletItemRenderer(){
        return new IClientItemExtensions() {
            final BlockEntityWithoutLevelRenderer renderer = new SkilletItemRenderer();
            @Override public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        };
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        var skilletItemRenderer = buildSkilletItemRenderer();
        SDSkilletItems.SKILLETS.forEach((m, skillet) -> event.registerItem(skilletItemRenderer, skillet.get()));
        event.registerItem(skilletItemRenderer, SDSkilletItems.FARMER.get());
    }
}

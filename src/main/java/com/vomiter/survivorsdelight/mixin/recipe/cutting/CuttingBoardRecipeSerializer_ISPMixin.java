package com.vomiter.survivorsdelight.mixin.recipe.cutting;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.util.ISPConverter;
import com.vomiter.survivorsdelight.data.recipe.SDCuttingRecipe;
import net.dries007.tfc.common.recipes.outputs.ItemStackProvider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = CuttingBoardRecipe.Serializer.class, remap = false)
public abstract class CuttingBoardRecipeSerializer_ISPMixin {

    @Inject(
            method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lvectorwing/farmersdelight/common/crafting/CuttingBoardRecipe;",
            at = @At("HEAD"), cancellable = true
    )
    private void sd$fromJson(ResourceLocation id, JsonObject json, CallbackInfoReturnable<CuttingBoardRecipe> cir) {
        final String group = GsonHelper.getAsString(json, "group", "");

        final NonNullList<Ingredient> inputs = NonNullList.create();
        JsonArray ingArr = GsonHelper.getAsJsonArray(json, "ingredients");
        for (JsonElement e : ingArr) {
            Ingredient ing = Ingredient.fromJson(e);
            if (!ing.isEmpty()) inputs.add(ing);
        }
        if (inputs.size() != 1) return; //理論上砧板只會有一格輸入

        // FD 1.3+ writes "tool" as an array of alternatives; read it the way FD's own serializer does
        final JsonElement toolJson = GsonHelper.isArrayNode(json, "tool")
                ? GsonHelper.getAsJsonArray(json, "tool")
                : GsonHelper.getAsJsonObject(json, "tool");
        final Ingredient tool = Ingredient.fromJson(toolJson);
        if (tool.isEmpty()) return;

        final String sound = GsonHelper.getAsString(json, "sound", "");

        // 讀 result：同時支援 FD ChanceResult 與 TFC ISP
        final NonNullList<ChanceResult> vanilla = NonNullList.create();
        final List<ItemStackProvider> providers = new ArrayList<>();
        boolean hasProvider = false;

        JsonArray resArr = GsonHelper.getAsJsonArray(json, "result");
        for (JsonElement el : resArr) {
            JsonObject obj = el.getAsJsonObject();
            if (obj.has("modifiers")) {
                hasProvider = true;
                providers.add(ISPConverter.providerFromLooseJson(obj)); // 轉成 ISP 期望格式
            } else if (obj.has("stack")) {
                hasProvider = true;
                providers.add(ItemStackProvider.fromJson(obj));
            } else {
                vanilla.add(ChanceResult.deserialize(obj));
            }
        }

        if (vanilla.size() + providers.size() > CuttingBoardRecipe.MAX_RESULTS) {
            // 上限限制（4）
            return;
        }

        if (hasProvider) {
            cir.setReturnValue(new SDCuttingRecipe(
                    id, group, inputs.get(0), tool, vanilla, providers, sound
            ));
        }
        // 若沒有 provider，就不攔截，讓 FD 原法照舊建立 CuttingBoardRecipe
    }

    @Inject(
            method = "fromNetwork(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lvectorwing/farmersdelight/common/crafting/CuttingBoardRecipe;",
            at = @At("HEAD"), cancellable = true
    )
    private void sdtfc$getFromNetwork(ResourceLocation id, FriendlyByteBuf buf, CallbackInfoReturnable<CuttingBoardRecipe> cir) {
        final String group = buf.readUtf(32767);
        final Ingredient input = Ingredient.fromNetwork(buf);
        final Ingredient tool = Ingredient.fromNetwork(buf);

        final boolean hasProvider = buf.readBoolean(); // ← 我們插入的旗標

        final int vanillaCount = buf.readVarInt();
        final NonNullList<ChanceResult> vanilla = NonNullList.withSize(vanillaCount, ChanceResult.EMPTY);
        for (int i = 0; i < vanillaCount; i++) vanilla.set(i, ChanceResult.read(buf));

        final List<ItemStackProvider> providers = new ArrayList<>();
        if (hasProvider) {
            final int pSize = buf.readVarInt();
            for (int i = 0; i < pSize; i++) providers.add(ItemStackProvider.fromNetwork(buf));
        }

        final String sound = buf.readUtf(); // 注意：FD 原本把 sound 放最後；我們保持一致

        if (hasProvider) {
            cir.setReturnValue(new SDCuttingRecipe(
                    id, group, input, tool, vanilla, providers, sound
            ));
        } else {
            cir.setReturnValue(new CuttingBoardRecipe(id, group, input, tool, vanilla, sound));
        }
    }

    @Inject(
            method = "toNetwork(Lnet/minecraft/network/FriendlyByteBuf;Lvectorwing/farmersdelight/common/crafting/CuttingBoardRecipe;)V",
            at = @At("HEAD"), cancellable = true
    )
    private void sdtfc$toNetwork(FriendlyByteBuf buf, CuttingBoardRecipe recipe, CallbackInfo ci) {
        buf.writeUtf(recipe.getGroup());
        recipe.getIngredients().get(0).toNetwork(buf); // 只有單一 input
        recipe.getTool().toNetwork(buf);

        if (recipe instanceof SDCuttingRecipe ext) {
            buf.writeBoolean(true); // hasProvider
            // 先寫 FD 的 results
            final NonNullList<ChanceResult> vanilla = recipe.getRollableResults();
            buf.writeVarInt(vanilla.size());
            for (ChanceResult r : vanilla) r.write(buf);
            // 再寫 providers
            final List<ItemStackProvider> providers = ext.getProviders();
            buf.writeVarInt(providers.size());
            for (ItemStackProvider isp : providers) isp.toNetwork(buf);
            // sound 放最後（保持與 FD 相同）
            buf.writeUtf(recipe.getSoundEventID());
            ci.cancel();
            return;
        }

        // 沒 provider：寫 hasProvider=false，其他與 FD 相同
        buf.writeBoolean(false);
        final NonNullList<ChanceResult> vanilla = recipe.getRollableResults();
        buf.writeVarInt(vanilla.size());
        for (ChanceResult r : vanilla) r.write(buf);
        buf.writeUtf(recipe.getSoundEventID());
        ci.cancel();
    }
}

package com.vomiter.survivorsdelight.mixin.recipe.cooking;

import com.google.gson.JsonObject;
import com.vomiter.survivorsdelight.adapter.cooking_pot.fluid.IFluidRequiringRecipe;
import net.dries007.tfc.common.recipes.ingredients.FluidStackIngredient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;

import java.util.Objects;

@Mixin(value = CookingPotRecipe.Serializer.class, remap = false)
public abstract class CookingPotRecipe_Serializer_FluidMixin {

    // JSON -> Recipe：在 RETURN 後把 fluid 欄位寫進剛產生的配方
    @Inject(method = "fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;)Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;", at = @At("RETURN"))
    private void sdtfc$readFluidFromJson(ResourceLocation id, JsonObject json, CallbackInfoReturnable<CookingPotRecipe> cir) {
        if (!json.has("fluid")) return;
        JsonObject f = json.getAsJsonObject("fluid");
        FluidStackIngredient ing = FluidStackIngredient.fromJson(f);
        int amount = f.has("amount") ? Math.max(0, f.get("amount").getAsInt()) : 0;
        if (amount > 0) {
            ((IFluidRequiringRecipe) cir.getReturnValue()).sdtfc$setFluidRequirement(ing, amount);
        }
    }

    // Network -> Recipe：在 RETURN 後讀回 fluid 欄位
    @Inject(method = "fromNetwork(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/network/FriendlyByteBuf;)Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;", at = @At("RETURN"))
    private void sdtfc$readFluidFromNet(ResourceLocation id, FriendlyByteBuf buf, CallbackInfoReturnable<CookingPotRecipe> cir) {
        boolean hasFluid = buf.readBoolean();
        if (!hasFluid) return;
        FluidStackIngredient ing = FluidStackIngredient.fromNetwork(buf);
        int amount = buf.readVarInt();
        if (amount > 0) {
            ((IFluidRequiringRecipe) cir.getReturnValue()).sdtfc$setFluidRequirement(ing, amount);
        }
    }

    // Recipe -> Network：在原本寫完後補寫 fluid 欄位
    @Inject(method = "toNetwork(Lnet/minecraft/network/FriendlyByteBuf;Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;)V", at = @At("TAIL"))
    private void sdtfc$writeFluidToNet(FriendlyByteBuf buf, CookingPotRecipe recipe, CallbackInfo ci) {
        IFluidRequiringRecipe duck = (IFluidRequiringRecipe) recipe;
        boolean has = duck.sdtfc$getFluidIngredient() != null && duck.sdtfc$getRequiredFluidAmount() > 0;
        buf.writeBoolean(has);
        if (has) {
            Objects.requireNonNull(duck.sdtfc$getFluidIngredient()).toNetwork(buf);
            buf.writeVarInt(duck.sdtfc$getRequiredFluidAmount());
        }
    }
}
package com.vomiter.survivorsdelight.mixin.recipe;

import com.google.gson.JsonElement;
import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = RecipeManager.class, priority = 5000)
public abstract class RecipeManager_ApplyDebugProbeMixin {

    // --- "print once" gates (to avoid log spam) ---
    private static final AtomicBoolean ONCE_HEAD = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_TAIL = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_RETURN = new AtomicBoolean(false);

    private static final AtomicBoolean ONCE_LOOP_ENTER = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_SKIP_UNDERSCORE = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_COND_BEFORE = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_COND_AFTER = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_COND_SKIP = new AtomicBoolean(false);

    private static final AtomicBoolean ONCE_FROMJSON_BEFORE = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_FROMJSON_AFTER = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_RECIPE_NULL = new AtomicBoolean(false);

    private static final AtomicBoolean ONCE_CATCH_ERROR_BEFORE = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_CATCH_ERROR_AFTER = new AtomicBoolean(false);

    private static final AtomicBoolean ONCE_PUTFIELD_RECIPES = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_PUTFIELD_BYNAME = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_LOADED_LOG_BEFORE = new AtomicBoolean(false);
    private static final AtomicBoolean ONCE_LOADED_LOG_AFTER = new AtomicBoolean(false);

    // --- lightweight counters (per apply call) ---
    private int sd$seenEntries = 0;
    private int sd$skippedUnderscore = 0;
    private int sd$skippedConditions = 0;
    private int sd$recipeNull = 0;
    private int sd$caughtParseErrors = 0;

    private void sd$logOnce(AtomicBoolean gate, String msg, Object... args) {
        if (gate.compareAndSet(false, true)) {
            SurvivorsDelight.LOGGER.info(msg, args);
        }
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"),
            require = 1
    )
    private void sd$probe_apply_HEAD(Map<ResourceLocation, JsonElement> json,
                                     ResourceManager rm,
                                     ProfilerFiller profiler,
                                     CallbackInfo ci) {
        // reset counters each call
        sd$seenEntries = 0;
        sd$skippedUnderscore = 0;
        sd$skippedConditions = 0;
        sd$recipeNull = 0;
        sd$caughtParseErrors = 0;

        sd$logOnce(ONCE_HEAD, "[SD][Probe] RecipeManager.apply HEAD hit (jsonSize={})", json.size());
        SurvivorsDelight.LOGGER.info("[SD][Probe] apply HEAD (this call) jsonSize={}", json.size());
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void sd$probe_apply_TAIL(Map<ResourceLocation, JsonElement> json,
                                     ResourceManager rm,
                                     ProfilerFiller profiler,
                                     CallbackInfo ci) {
        sd$logOnce(ONCE_TAIL, "[SD][Probe] RecipeManager.apply TAIL reached");
        SurvivorsDelight.LOGGER.info("[SD][Probe] apply TAIL (this call) reached; counters: seen={} _skip={} condSkip={} null={} catch={}",
                sd$seenEntries, sd$skippedUnderscore, sd$skippedConditions, sd$recipeNull, sd$caughtParseErrors);
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("RETURN"),
            require = 0
    )
    private void sd$probe_apply_RETURN(Map<ResourceLocation, JsonElement> json,
                                       ResourceManager rm,
                                       ProfilerFiller profiler,
                                       CallbackInfo ci) {
        sd$logOnce(ONCE_RETURN, "[SD][Probe] RecipeManager.apply RETURN reached");
        SurvivorsDelight.LOGGER.info("[SD][Probe] apply RETURN (this call) reached; counters: seen={} _skip={} condSkip={} null={} catch={}",
                sd$seenEntries, sd$skippedUnderscore, sd$skippedConditions, sd$recipeNull, sd$caughtParseErrors);
    }

    // ---- Loop progress: hook Map.entrySet() call (close to loop start) ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"),
            require = 0
    )
    private void sd$probe_before_entrySet(Map<ResourceLocation, JsonElement> json,
                                          ResourceManager rm,
                                          ProfilerFiller profiler,
                                          CallbackInfo ci) {
        sd$logOnce(ONCE_LOOP_ENTER, "[SD][Probe] about to iterate json.entrySet()");
    }

    // ---- Underscore skip: hook ResourceLocation.getPath() before startsWith("_") is evaluated ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;getPath()Ljava/lang/String;"),
            require = 0
    )
    private void sd$probe_on_getPath(Map<ResourceLocation, JsonElement> json,
                                     ResourceManager rm,
                                     ProfilerFiller profiler,
                                     CallbackInfo ci) {
        // This INVOKE happens for every entry; avoid spamming.
        // We'll only print once here; actual counting is better done elsewhere,
        // but we cannot safely read the current entry id from here without locals capture.
        sd$logOnce(ONCE_SKIP_UNDERSCORE, "[SD][Probe] ResourceLocation.getPath() invoked (underscore filter section is executing)");
    }

    // ---- Conditions processing: before/after CraftingHelper.processConditions(...) ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/crafting/CraftingHelper;processConditions(Lcom/google/gson/JsonObject;Ljava/lang/String;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Z"),
            require = 0
    )
    private void sd$probe_before_processConditions(Map<ResourceLocation, JsonElement> json,
                                                   ResourceManager rm,
                                                   ProfilerFiller profiler,
                                                   CallbackInfo ci) {
        sd$logOnce(ONCE_COND_BEFORE, "[SD][Probe] BEFORE CraftingHelper.processConditions(...)");
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/crafting/CraftingHelper;processConditions(Lcom/google/gson/JsonObject;Ljava/lang/String;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Z", shift = At.Shift.AFTER),
            require = 0
    )
    private void sd$probe_after_processConditions(Map<ResourceLocation, JsonElement> json,
                                                  ResourceManager rm,
                                                  ProfilerFiller profiler,
                                                  CallbackInfo ci) {
        sd$logOnce(ONCE_COND_AFTER, "[SD][Probe] AFTER CraftingHelper.processConditions(...)");
    }

    // ---- fromJson call site: before/after RecipeManager.fromJson(...) ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/world/item/crafting/Recipe;"),
            require = 0
    )
    private void sd$probe_before_fromJson(Map<ResourceLocation, JsonElement> json,
                                          ResourceManager rm,
                                          ProfilerFiller profiler,
                                          CallbackInfo ci) {
        sd$logOnce(ONCE_FROMJSON_BEFORE, "[SD][Probe] BEFORE RecipeManager.fromJson(...)");
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/RecipeManager;fromJson(Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonObject;Lnet/minecraftforge/common/crafting/conditions/ICondition$IContext;)Lnet/minecraft/world/item/crafting/Recipe;", shift = At.Shift.AFTER),
            require = 0
    )
    private void sd$probe_after_fromJson(Map<ResourceLocation, JsonElement> json,
                                         ResourceManager rm,
                                         ProfilerFiller profiler,
                                         CallbackInfo ci) {
        sd$logOnce(ONCE_FROMJSON_AFTER, "[SD][Probe] AFTER RecipeManager.fromJson(...)");
    }

    // ---- Catch block signal: hook the LOGGER.error(...) in catch ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"),
            require = 0
    )
    private void sd$probe_before_loggerError(Map<ResourceLocation, JsonElement> json,
                                             ResourceManager rm,
                                             ProfilerFiller profiler,
                                             CallbackInfo ci) {
        sd$logOnce(ONCE_CATCH_ERROR_BEFORE, "[SD][Probe] ENTER catch(IllegalArgumentException|JsonParseException) path (before LOGGER.error)");
        sd$caughtParseErrors++; // approximate; counts first hit reliably, may undercount if signature differs
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V", shift = At.Shift.AFTER),
            require = 0
    )
    private void sd$probe_after_loggerError(Map<ResourceLocation, JsonElement> json,
                                            ResourceManager rm,
                                            ProfilerFiller profiler,
                                            CallbackInfo ci) {
        sd$logOnce(ONCE_CATCH_ERROR_AFTER, "[SD][Probe] EXIT catch path (after LOGGER.error)");
    }

    // ---- Field write checkpoints: when apply writes this.recipes / this.byName ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/RecipeManager;recipes:Ljava/util/Map;", opcode = org.objectweb.asm.Opcodes.PUTFIELD),
            require = 0
    )
    private void sd$probe_putfield_recipes(Map<ResourceLocation, JsonElement> json,
                                           ResourceManager rm,
                                           ProfilerFiller profiler,
                                           CallbackInfo ci) {
        sd$logOnce(ONCE_PUTFIELD_RECIPES, "[SD][Probe] PUTFIELD this.recipes is happening");
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/crafting/RecipeManager;byName:Ljava/util/Map;", opcode = org.objectweb.asm.Opcodes.PUTFIELD),
            require = 0
    )
    private void sd$probe_putfield_byName(Map<ResourceLocation, JsonElement> json,
                                          ResourceManager rm,
                                          ProfilerFiller profiler,
                                          CallbackInfo ci) {
        sd$logOnce(ONCE_PUTFIELD_BYNAME, "[SD][Probe] PUTFIELD this.byName is happening");
    }

    // ---- The final Loaded log: hook before/after LOGGER.info("Loaded {} recipes", ...) ----
    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V"),
            require = 0
    )
    private void sd$probe_before_loadedInfo(Map<ResourceLocation, JsonElement> json,
                                            ResourceManager rm,
                                            ProfilerFiller profiler,
                                            CallbackInfo ci) {
        sd$logOnce(ONCE_LOADED_LOG_BEFORE, "[SD][Probe] about to call LOGGER.info(\"Loaded {} recipes\", ...)");
    }

    @Inject(
            method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", shift = At.Shift.AFTER),
            require = 0
    )
    private void sd$probe_after_loadedInfo(Map<ResourceLocation, JsonElement> json,
                                           ResourceManager rm,
                                           ProfilerFiller profiler,
                                           CallbackInfo ci) {
        sd$logOnce(ONCE_LOADED_LOG_AFTER, "[SD][Probe] after LOGGER.info(\"Loaded {} recipes\", ...)");
    }
}
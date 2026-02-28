package com.vomiter.survivorsdelight.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MixinGate implements IMixinConfigPlugin {
    private static boolean isDataGen() {
        return "true".equalsIgnoreCase(System.getProperty("survivorsdelight.datagen"))
                || "true".equalsIgnoreCase(System.getProperty("forge.datagen")); // FG 也會帶這旗標
    }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(isDataGen()) return false;
        if(!mixinClassName.contains("compat")) return true;
        if(shouldBlockCompatMixin( mixinClassName,"firmalife")) return false;
        if(shouldBlockCompatMixin( mixinClassName,"rosia")) return false;
        if(shouldBlockCompatMixin( mixinClassName,"immersiveengineering")) return false;

        return !isDataGen(); // datagen 全部不套用 mixin
    }

    boolean shouldBlockCompatMixin(String mixinClassName, String modId){
        return mixinClassName.contains(modId) && (LoadingModList.get().getModFileById(modId) == null);
    }

    @Override public void onLoad(String mixinPackage) {}
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}

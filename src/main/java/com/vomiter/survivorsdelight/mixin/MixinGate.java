package com.vomiter.survivorsdelight.mixin;

import com.vomiter.survivorsdelight.SurvivorsDelight;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public final class MixinGate implements IMixinConfigPlugin {
    private static final String FD_MODID = "farmersdelight";

    private static boolean isDataGen() {
        return "true".equalsIgnoreCase(System.getProperty("survivorsdelight.datagen"))
                || "true".equalsIgnoreCase(System.getProperty("forge.datagen"));
    }
    @Override public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(isDataGen()) return false;
        if (!mixinClassName.contains("compat")) {
            if (mixinClassName.endsWith("PieBlock_SliceMixin")) {
                return isModVersionInRange(FD_MODID, "[1.20.1-1.2.11,)");
            }
            if (mixinClassName.endsWith("PieBlock_SliceLegacyMixin")) {
                return isModVersionInRange(FD_MODID, "[0,1.20.1-1.2.10]");
            }
            return true;
        }
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

    private static boolean isModVersionInRange(String modId, String rangeSpec) {
        var modFile = LoadingModList.get().getModFileById(modId);
        if (modFile == null) return false;

        List<IModInfo> mods = modFile.getMods();
        if (mods.isEmpty()) return false;

        ArtifactVersion version = mods.get(0).getVersion();
        try {
            VersionRange range = VersionRange.createFromVersionSpec(rangeSpec);
            SurvivorsDelight.LOGGER.info("Current Farmer's Delight Version = {}, Testing Range = {}, Testing Result = {}",
                    version,
                    range,
                    range.containsVersion(version)
            );
            return range.containsVersion(version);
        } catch (InvalidVersionSpecificationException e) {
            SurvivorsDelight.LOGGER.warn("Invalid farmer's delight version!");
            return false;
        }
    }
}

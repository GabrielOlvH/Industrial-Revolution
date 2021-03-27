package me.steven.indrev.mixin.aprilfools;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AprilFoolsMixinConfigPlugin implements IMixinConfigPlugin {
    private static final LocalDate APRIL_FOOLS = LocalDate.parse("2021-04-01");
    private static final ArrayList<String> mixins = new ArrayList<>();
    static {
        mixins.add("aprilfools.MixinTranslatableText");
    }

    private boolean isAprilFools() {
        return LocalDate.now().equals(APRIL_FOOLS);
    }

    @Override
    public void onLoad(String mixinPackage) { }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) { }

    @Override
    public List<String> getMixins() {
        return isAprilFools()? mixins : null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) { }
}

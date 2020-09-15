package me.steven.indrev.mixin;

import net.minecraft.util.collection.WeightedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(WeightedList.class)
public interface AccessorWeightedList<U> {
    @Accessor
    List<WeightedList.Entry<U>> getEntries();
}

package me.steven.indrev.mixin.common;

import net.minecraft.item.Item;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.RequiredTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemTags.class)
public interface AccessorItemTags {
    @Accessor("REQUIRED_TAGS")
    static RequiredTagList<Item> getRequiredTagList() {
        throw new AssertionError();
    }
}

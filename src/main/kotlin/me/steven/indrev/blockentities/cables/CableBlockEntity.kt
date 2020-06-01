package me.steven.indrev.blockentities.cables

import me.steven.indrev.blockentities.BasicMachineBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier

class CableBlockEntity(tier: Tier) : BasicMachineBlockEntity(MachineRegistry.CABLE_REGISTRY[tier], tier, 16.0) {
    var cover: Identifier? = null

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun fromTag(tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover = Identifier(tag.getString("cover"))
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("cover", cover.toString())
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover = Identifier(tag.getString("cover"))
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("cover", cover.toString())
        return super.toClientTag(tag)
    }
}
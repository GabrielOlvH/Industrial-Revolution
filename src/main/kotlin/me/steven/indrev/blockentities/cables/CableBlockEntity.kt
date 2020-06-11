package me.steven.indrev.blockentities.cables

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.Identifier

class CableBlockEntity(tier: Tier) :
    MachineBlockEntity(tier, MachineRegistry.CABLE_REGISTRY) {
    var cover: Identifier? = null

    init {
        this.propertyDelegate = ArrayPropertyDelegate(2)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover = Identifier(tag.getString("cover"))
        super.fromTag(state, tag)
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
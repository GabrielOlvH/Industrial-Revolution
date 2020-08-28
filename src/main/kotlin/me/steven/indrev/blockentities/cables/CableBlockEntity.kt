package me.steven.indrev.blockentities.cables

import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier

class CableBlockEntity(val tier: Tier) :
    BlockEntity(MachineRegistry.CABLE_REGISTRY.blockEntityType(tier)), BlockEntityClientSerializable {
    var cover: Identifier? = null

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
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        tag.putString("cover", cover.toString())
        return tag
    }
}
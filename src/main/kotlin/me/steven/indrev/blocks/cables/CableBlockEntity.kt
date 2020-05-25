package me.steven.indrev.blocks.cables

import me.steven.indrev.blocks.BasicMachineBlockEntity
import me.steven.indrev.content.MachineRegistry
import net.minecraft.container.ArrayPropertyDelegate
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import team.reborn.energy.EnergySide

class CableBlockEntity : BasicMachineBlockEntity(MachineRegistry.CABLE_BLOCK_ENTITY, 10.0) {
    var cover: Identifier? = null

    override fun createDelegate(): PropertyDelegate = ArrayPropertyDelegate(2)

    override fun getMaxInput(side: EnergySide?): Double = 32.0

    override fun getMaxOutput(side: EnergySide?): Double = 32.0

    override fun fromTag(tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover =  Identifier(tag.getString("cover"))
        super.fromTag(tag)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("cover", cover.toString())
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        if (tag?.contains("cover") == true)
            cover =  Identifier(tag.getString("cover"))
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putString("cover", cover.toString())
        return super.toClientTag(tag)
    }
}
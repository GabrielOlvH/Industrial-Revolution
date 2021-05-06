package me.steven.indrev.blockentities.solarpowerplant

import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos

class SolarReceiverBlockEntity : BlockEntity(IRBlockRegistry.SOLAR_RECEIVER_BLOCK_ENTITY) {
    var controllerPos: BlockPos = BlockPos.ORIGIN

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putLong("controller", controllerPos.asLong())
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        controllerPos = BlockPos.fromLong(tag?.getLong("controller") ?: -1)
    }
}
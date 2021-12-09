package me.steven.indrev.blockentities.solarpowerplant

import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class SolarReceiverBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(IRBlockRegistry.SOLAR_RECEIVER_BLOCK_ENTITY, pos, state) {
    var controllerPos: BlockPos = BlockPos.ORIGIN

    override fun writeNbt(tag: NbtCompound?) {
        tag?.putLong("controller", controllerPos.asLong())
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        controllerPos = BlockPos.fromLong(tag.getLong("controller"))
    }
}
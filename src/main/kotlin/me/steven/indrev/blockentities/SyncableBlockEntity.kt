package me.steven.indrev.blockentities

import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos

abstract class SyncableBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state), Syncable {
    var isMarkedForUpdate: Boolean = true

    override fun markForUpdate(condition: () -> Boolean) {
        isMarkedForUpdate = isMarkedForUpdate || condition()
    }
}
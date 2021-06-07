package me.steven.indrev.blockentities

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos

abstract class IRSyncableBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state), BlockEntityClientSerializable {
    var isMarkedForUpdate: Boolean = true

    fun markForUpdate(condition: () -> Boolean = { true }) {
        isMarkedForUpdate = isMarkedForUpdate || condition()
    }
}
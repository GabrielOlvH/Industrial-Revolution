package me.steven.indrev.blockentities

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType

abstract class IRSyncableBlockEntity(type: BlockEntityType<*>) : BlockEntity(type), BlockEntityClientSerializable {
    var isMarkedForUpdate: Boolean = true

    fun markForUpdate(condition: () -> Boolean = { true }) {
        isMarkedForUpdate = isMarkedForUpdate || condition()
    }
}
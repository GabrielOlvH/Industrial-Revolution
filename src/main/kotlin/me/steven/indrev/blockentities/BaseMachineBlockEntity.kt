package me.steven.indrev.blockentities

import me.steven.indrev.api.sideconfigs.Configurable
import me.steven.indrev.components.ComponentProvider
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.util.math.BlockPos

abstract class BaseMachineBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState)
    : BaseBlockEntity(type, pos, state), InventoryProvider, Configurable, ComponentProvider, Syncable {
    var isMarkedForUpdate: Boolean = true

    override fun markForUpdate(condition: () -> Boolean) {
        isMarkedForUpdate = isMarkedForUpdate || condition()
    }
}
package me.steven.indrev.utils

import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage

@Suppress("CAST_NEVER_SUCCEEDS")
object EnergyMovement {
    fun spreadNeighbors(sourceBlockEntity: BlockEntity, pos: BlockPos) {
        if (sourceBlockEntity !is EnergyStorage) return
        val world = sourceBlockEntity.world
        val sourceHandler = Energy.of(sourceBlockEntity)
        Direction.values().forEach { direction ->
            if (sourceBlockEntity.getMaxOutput(EnergySide.fromMinecraft(direction)) > 0) {
                val targetPos = pos.offset(direction)
                val target = world?.getBlockEntity(targetPos)
                if (target != null && Energy.valid(target)) {
                    val targetHandler = Energy.of(target).side(direction.opposite)
                    sourceHandler.into(targetHandler).move()
                }
            }
        }
    }
}
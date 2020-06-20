package me.steven.indrev.utils

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.mixin.AccessorEnergyHandler
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage

@Suppress("CAST_NEVER_SUCCEEDS")
object EnergyMovement {
    fun spreadNeighbors(sourceBlockEntity: BlockEntity, pos: BlockPos) {
        val world = sourceBlockEntity.world
        if (sourceBlockEntity !is EnergyStorage) return
        val sourceHandler = Energy.of(sourceBlockEntity)
        val targets = Direction.values()
            .mapNotNull { direction ->
                if (sourceBlockEntity is MachineBlockEntity && sourceBlockEntity.lastInputFrom == direction) return@mapNotNull null
                else if (sourceBlockEntity.getMaxOutput(EnergySide.fromMinecraft(direction)) > 0) {
                    val targetPos = pos.offset(direction)
                    val target = world?.getBlockEntity(targetPos)
                    if (target != null && Energy.valid(target)) {
                        val targetHandler = Energy.of(target).side(direction.opposite)
                        if (targetHandler.energy < targetHandler.maxStored)
                            return@mapNotNull targetHandler
                    }
                }
                null
            }
        val sum = targets.sumByDouble { targetHandler ->
            (targetHandler.maxStored - targetHandler.energy).coerceAtMost(targetHandler.maxInput)
        }
        targets.forEach { targetHandler ->
            val accessor = targetHandler as AccessorEnergyHandler
            val direction = accessor.side
            val target = accessor.holder
            sourceHandler.side(direction)
            val targetMaxInput = targetHandler.maxInput
            val amount = (targetMaxInput / sum) * sourceHandler.energy
            if (amount > 0) {
                if (target is MachineBlockEntity)
                    target.lastInputFrom = Direction.byName(direction.toString())
                sourceHandler.into(targetHandler).move(amount)
            }
        }
    }
}
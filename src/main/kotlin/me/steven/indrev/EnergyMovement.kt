package me.steven.indrev

import me.steven.indrev.blockentities.MachineBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergyHolder
import team.reborn.energy.EnergySide

class EnergyMovement(private val sourceBlockEntity: BlockEntity, private val pos: BlockPos) {
    fun spread(vararg directions: Direction) {
        val world = sourceBlockEntity.world
        val sourceState = sourceBlockEntity.cachedState
        if (sourceBlockEntity !is EnergyHolder) return
        val handler = Energy.of(sourceBlockEntity)
        directions
            .associate { direction ->
                if (sourceBlockEntity.getMaxOutput(EnergySide.fromMinecraft(direction)) <= 0) return@associate Pair(null, null)
                val targetPos = pos.offset(direction)
                val target = world!!.getBlockEntity(targetPos)
                if (target == null || !Energy.valid(target)) return@associate Pair(null, null)
                val targetHandler = Energy.of(target).side(direction.opposite)
                if (targetHandler.energy >= targetHandler.maxStored) Pair(null, null)
                else Pair(direction, Pair(target, targetHandler))
            }
            .filter { (left, right) -> left != null && right != null }
            .apply {
                val sum = values.sumByDouble { pair ->
                    val targetHandler = pair!!.second
                    (targetHandler.maxStored - targetHandler.energy).coerceAtMost(targetHandler.maxInput)
                }
                forEach { pair ->
                    val target = pair.value?.first
                    val targetHandler = pair.value?.second
                    val direction = pair.key
                    handler.side(direction)
                    val targetMaxInput = targetHandler!!.maxInput
                    val amount = (targetMaxInput / sum) * handler.maxOutput
                    if (amount > 0 && (sourceBlockEntity !is MachineBlockEntity || sourceBlockEntity.lastInputFrom != direction)) {
                        if (target is MachineBlockEntity)
                            target.lastInputFrom = direction?.opposite
                        handler.into(targetHandler).move(amount)
                    }
                }
            }
    }
}
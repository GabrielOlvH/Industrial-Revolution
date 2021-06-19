package me.steven.indrev.networks.energy

import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.energyOf
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

// This is used for machines connecting side to side.
@Suppress("CAST_NEVER_SUCCEEDS")
object IREnergyMovement {
    fun spreadNeighbors(source: MachineBlockEntity<*>, pos: BlockPos) {
        val world = source.world as ServerWorld
        Direction.values()
            .forEach { direction ->
                if (source.validConnections.contains(direction)) {
                    val sourceIo = energyOf(world, pos, direction)
                    val targetIo = energyOf(world, pos.offset(direction.opposite), direction.opposite)
                    if (sourceIo == null || targetIo == null)
                        source.validConnections.remove(direction)
                    else if (sourceIo.supportsExtraction() && targetIo.supportsInsertion())
                        EnergyMovement.move(sourceIo, targetIo, Double.MAX_VALUE)
                }
            }
    }
}
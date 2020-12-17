package me.steven.indrev.energy

import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

// This is used for machines connecting side to side.
@Suppress("CAST_NEVER_SUCCEEDS")
object IREnergyMovement {
    fun spreadNeighbors(source: BlockEntity, pos: BlockPos) {
        Direction.values()
            .forEach { direction ->
                val sourceIo = EnergyApi.SIDED[source.world, pos, direction]
                val targetIo = EnergyApi.SIDED[source.world, pos.offset(direction), direction.opposite]
                if (sourceIo != null && targetIo != null)
                    EnergyMovement.move(sourceIo, targetIo, Double.MAX_VALUE)
            }
    }
}
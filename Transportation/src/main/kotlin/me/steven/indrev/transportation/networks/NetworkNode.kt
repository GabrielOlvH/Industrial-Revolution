package me.steven.indrev.transportation.networks

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import me.steven.indrev.transportation.utils.PipeConnections
import net.minecraft.util.math.BlockPos

data class NetworkNode(val pos: BlockPos, val connections: PipeConnections, val connectionCount: Int, val distances: Long2IntOpenHashMap) {
    fun isCorner() = connectionCount == 1 || connectionCount > 2 || connections.getDirections().distinctBy { it.axis }.size > 1
}
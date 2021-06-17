package me.steven.indrev.networks.factory

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.blockentities.cables.BasePipeBlockEntity
import me.steven.indrev.networks.Network
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.chunk.Chunk

interface NetworkFactory<T : Network> {

    fun process(
        network: T,
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean

    fun deepScan(
        scanned: LongOpenHashSet,
        type: Network.Type<T>,
        network: T,
        chunk: Chunk,
        world: ServerWorld,
        blockPos: BlockPos,
        source: BlockPos,
        direction: Direction
    ) {
        val blockState by lazy { chunk.getBlockState(blockPos) }
        val shouldContinue = process(network, world, blockPos, direction.opposite) { blockState }
        val longPos = blockPos.asLong()
        if (blockPos != source && !scanned.add(longPos)) return
        if (shouldContinue) {
            if (type.networksByPos.containsKey(longPos)) {
                val oldNetwork = type.networksByPos[longPos]
                if (oldNetwork != network)
                    oldNetwork?.remove()
            }
            type.updatedPositions.add(longPos)
            val blockEntity = chunk.getBlockEntity(blockPos) as? BasePipeBlockEntity ?: return
            DIRECTIONS.forEach { dir ->
                if (blockEntity.connections[dir]!!.isConnected()) {
                    val nPos = blockPos.offset(dir)
                    if (nPos.x shr 4 == chunk.pos.x && nPos.z shr 4 == chunk.pos.z)
                        deepScan(scanned, type, network, chunk, world, nPos, source, dir)
                    else
                        deepScan(scanned, type, network, world.getChunk(nPos), world, nPos, source, dir)
                }
            }
        }
    }

    fun deepScan(
        type: Network.Type<T>,
        world: ServerWorld,
        source: BlockPos
    ): T {
        type
        val network = type.createEmpty(world)
        DIRECTIONS.forEach { direction ->
            deepScan(LongOpenHashSet(), type, network, world.getChunk(source), world, source, source, direction)
        }
        return network
    }

    companion object {
        val DIRECTIONS = Direction.values()
    }
}
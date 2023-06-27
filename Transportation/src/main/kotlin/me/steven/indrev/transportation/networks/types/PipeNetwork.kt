package me.steven.indrev.transportation.networks.types

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.api.Tier
import me.steven.indrev.transportation.blocks.PipeBlockEntity
import me.steven.indrev.transportation.networks.*
import me.steven.indrev.transportation.packets.AddPipeRenderDataPacket
import me.steven.indrev.transportation.utils.nested
import me.steven.indrev.transportation.utils.transaction
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class PipeNetwork<T>(val world: ServerWorld) {

    private var cachedPacket: PacketByteBuf? = null

    val apiCache = Long2ObjectOpenHashMap<BlockApiCache<T, Direction>>()

    var tier = Tier.MK1

    val nodes = Long2ObjectOpenHashMap<NetworkNode>()
    val containers = LongOpenHashSet()

    private val pathCache = Long2ObjectOpenHashMap<List<Path>>()
    private val submitted = LongOpenHashSet()

    var ticks = world.random.nextInt() % 20

    abstract val minimumTransferable: Long
    abstract val maximumTransferable: Long

    val tickingPositions = ObjectOpenHashSet<BlockPos>()

    var ready = false

    open fun tick() {
        ticks++
    }

    abstract fun find(world: ServerWorld, pos: BlockPos, direction: Direction): T?

    fun isValidStorage(world: ServerWorld, blockPos: BlockPos, direction: Direction): Boolean {
        return find(world, blockPos, direction) != null
    }

    fun getPathsFrom(pos: BlockPos): List<Path> {
        val longPos = pos.asLong()
        if (!pathCache.containsKey(longPos)) {
            if (!submitted.add(longPos)) return emptyList()

            EXECUTOR.submit {
                val paths = mutableListOf<Path>()
                containers.forEach { otherLongPos ->
                    if (otherLongPos != longPos) {
                        val path = mutableListOf<Long>()
                        val dst = findShortestPath(this, pos, BlockPos.fromLong(otherLongPos), path)
                        if (path.isNotEmpty())
                            paths.add(Path(path.toList(), dst))
                    }
                }
                world.server.execute { pathCache[longPos] = paths.sortedBy { it.dist } }
            }

            return emptyList()
        }
        return pathCache[longPos]
    }

    fun contains(pos: BlockPos) = nodes.contains(pos.asLong())

    fun sync(player: ServerPlayerEntity) {
        if (cachedPacket == null) {
            val buf = PacketByteBufs.create()
            buf.writeInt(nodes.size)
            nodes.forEach { (pos, node) ->
                buf.writeLong(pos)
                buf.writeInt(node.connections.value)
            }
            cachedPacket = buf
        }
        AddPipeRenderDataPacket.send(player, cachedPacket!!)
    }
}
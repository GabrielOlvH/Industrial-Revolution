package me.steven.indrev.transportation.networks

import com.google.common.util.concurrent.ThreadFactoryBuilder
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.transportation.blocks.PipeBlock
import me.steven.indrev.transportation.networks.types.PipeNetwork
import me.steven.indrev.transportation.utils.PipeConnections
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*
import java.util.concurrent.Executors

val EXECUTOR = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("IR Network Builder Thread %d").build())

private val DIRECTIONS = Direction.values()

fun update(world: ServerWorld, pos: BlockPos, newBlock: Block, block: PipeBlock) {
    val networkManager = world.networkManager
    if (newBlock == block) {
        createNetwork(networkManager, pos, world, block)
    } else {
        val oldNetwork = networkManager.networksByPos[pos.asLong()]
        if (oldNetwork != null) networkManager.remove(oldNetwork)
        for (dir in DIRECTIONS) {
            val offset = pos.offset(dir)
            if (world.getBlockState(offset).isOf(block)) {
                createNetwork(networkManager, offset, world, block)
            }
        }
    }
}

private fun createNetwork(manager: NetworkManager, pos: BlockPos, world: ServerWorld, block: PipeBlock) {
    val network = block.createNetwork(world)
    dfs(network, pos, world, block, LongOpenHashSet())
    if (network.nodes.isNotEmpty()) {
        manager.networks.add(network)
        manager.syncToAllPlayers(network)
        EXECUTOR.submit {
            calculateDistances(network)
            network.ready = true
        }
    }
}


fun dfs(network: PipeNetwork<*>, pos: BlockPos, world: ServerWorld, block: PipeBlock, searched: LongOpenHashSet) {
    if (!searched.add(pos.asLong())) return
    var connections = 0
    var neighborCount = 0
    val current = world.getBlockState(pos)
    for (dir in DIRECTIONS) {
        val offset = pos.offset(dir)
        val neighbor = world.getBlockState(offset)
        if (!neighbor.isOf(block)) {
            if (!network.isValidStorage(world, offset, dir.opposite)) continue
            else network.containers.add(offset.asLong())
        }
        dfs(network, offset, world, block, searched)

        if (current.isOf(block) || neighbor.isOf(block)) {
            connections = connections or (1 shl dir.id)
            neighborCount++
        }
    }
    network.nodes[pos.asLong()] = NetworkNode(pos, PipeConnections(connections), neighborCount, Long2IntOpenHashMap())
    val previous = world.networkManager.networksByPos.put(pos.asLong(), network)
    if (previous != null)
        world.networkManager.remove(previous)
}

fun calculateDistances(network: PipeNetwork<*>) {
    val mutable = BlockPos.Mutable()
    network.nodes.forEach { (pos, node) ->
        if (node.isCorner()) {
            node.distances.defaultReturnValue(Int.MAX_VALUE)
            mutable.set(pos)
            calculateDistances(network, node, node, mutable, 0, LongOpenHashSet(), node.distances)
        }
    }
}

fun calculateDistances(network: PipeNetwork<*>, rootNode: NetworkNode, node: NetworkNode, mutablePos: BlockPos.Mutable, count: Int, visited: LongOpenHashSet, distances: Long2IntOpenHashMap) {
    val nodePos = node.pos.asLong()
    if (!visited.add(nodePos)) return
    for (dir in DIRECTIONS) {
        if (node.connections.contains(dir)) {
            if (node.isCorner() && node != rootNode) {
                if (distances.get(nodePos) > count) {
                    distances.put(nodePos, count)
                    visited.remove(nodePos)
                }
            } else {
                mutablePos.set(node.pos.x + dir.offsetX, node.pos.y + dir.offsetY, node.pos.z + dir.offsetZ)
                val nextNode = network.nodes[mutablePos.asLong()]!!
                calculateDistances(network, rootNode, nextNode, mutablePos, count + 1, visited, distances)
            }
        }
    }
}

fun findShortestPath(network: PipeNetwork<*>, pos1: BlockPos, pos2: BlockPos, path: MutableList<Long>): Int {
    val dist = Long2IntOpenHashMap()
    dist.defaultReturnValue(9999)
    val visited = LongOpenHashSet()
    val orig = pos1.asLong()
    val dest = pos2.asLong()

    val previous = Long2LongOpenHashMap()

    val pq = PriorityQueue<PathNode>(compareBy { it.dist })
    dist.put(orig, 0)
    pq.add(PathNode(orig, 0))
    while (pq.isNotEmpty()) {
        val (currentNode, _) = pq.poll()

        if (visited.add(currentNode)) {
            network.nodes[currentNode].distances.forEach { (adj, d) ->
                if (dist.get(adj) > (dist.get(currentNode) + d)) {
                    dist[adj] = dist.get(currentNode) + d
                    pq.offer(PathNode(adj, dist.get(adj)))
                    previous[adj] = currentNode
                }
            }
        }
    }
    if (!visited.contains(dest)) return -1
    path.add(dest)

    var s = dest
    while (s != orig) {
        s = previous[s]
        path.add(s)
    }

    return dist.get(dest)
}


data class PathNode(val pos: Long, val dist: Int)
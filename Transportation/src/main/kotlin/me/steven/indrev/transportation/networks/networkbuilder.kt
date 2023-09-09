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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

val EXECUTOR: ExecutorService = Executors.newCachedThreadPool(ThreadFactoryBuilder().setNameFormat("IR Network Builder Thread %d").build())

private val DIRECTIONS = Direction.values()

fun update(world: ServerWorld, pos: BlockPos, newBlock: Block, block: PipeBlock) {
    val networkManager = world.networkManager
    var oldNetwork = networkManager.networksByPos[pos.asLong()]
    if (oldNetwork != null) networkManager.remove(oldNetwork)

    if (newBlock == block) {
        networkManager.scheduledUpdates.add(pos.asLong())
        //createNetwork(networkManager, pos, world, block)
    } else {
        for (dir in DIRECTIONS) {
            val offset = pos.offset(dir)

            oldNetwork = networkManager.networksByPos[offset.asLong()]
            if (oldNetwork != null) networkManager.remove(oldNetwork)
        }

        for (dir in DIRECTIONS) {
            val offset = pos.offset(dir)

            if (world.getBlockState(offset).isOf(block)) {
                networkManager.scheduledUpdates.add(offset.asLong())
                //createNetwork(networkManager, offset, world, block)
            }
        }
    }
}

fun createNetwork(manager: NetworkManager, pos: BlockPos, world: ServerWorld, block: PipeBlock) {
    val network = block.createNetwork(world)
    network.tier = block.tier
    dfs(manager, network, pos, world, block, LongOpenHashSet())
    if (network.nodes.isNotEmpty()) {
        manager.networks.add(network)
        network.nodes.forEach { (pos, _) ->
            if (!network.containers.contains(pos)) {
                world.networkManager.networksByPos.put(pos, network)
            }
        }
        manager.syncToAllPlayers(network)
        EXECUTOR.submit {
            calculateDistances(network)
            network.ready = true
        }
    }
}

fun dfs2(network: PipeNetwork<*>, pos: BlockPos, world: ServerWorld, pipe: PipeBlock, searched: LongOpenHashSet) {
    val longPos = pos.asLong()
    if (!searched.add(longPos)) return

    var connections = PipeConnections(0)
    var connCount = 0
    val block = world.getBlockState(pos)
    for (dir in DIRECTIONS) {
        val offset = pos.offset(dir)

        if (block.isOf(pipe)) {
            dfs2(network, offset, world, pipe, searched)
        } else {
            continue
        }

        val neighbor = world.getBlockState(offset)
        if (neighbor.isOf(pipe) || network.isValidStorage(world, offset, dir.opposite)) {
            connections = connections.with(dir)
            connCount++
        }
    }
    if (connCount > 0)
        network.nodes[longPos] = NetworkNode(pos, connections, connCount, Long2IntOpenHashMap())
}

fun dfs(manager: NetworkManager, network: PipeNetwork<*>, pos: BlockPos, world: ServerWorld, block: PipeBlock, searched: LongOpenHashSet) {
    if (!searched.add(pos.asLong())) return
    var connections = 0
    var neighborCount = 0
    //val current = world.getBlockState(pos)

    for (dir in DIRECTIONS) {
        val offset = pos.offset(dir)

        val neighbor = world.getBlockState(offset)
        if (neighbor.isOf(block))
            dfs(manager, network, offset, world, block, searched)
        else if (network.isValidStorage(world, offset, dir.opposite)) {
            network.containers.add(offset.asLong())
            if (network.nodes.contains(offset.asLong())) {
                val node = network.nodes[offset.asLong()]
                network.nodes[offset.asLong()] = NetworkNode(offset, node.connections.with(dir.opposite), node.connectionCount + 1, node.distances)
            } else {
                network.nodes[offset.asLong()] = NetworkNode(offset, PipeConnections(0).with(dir.opposite), 1, Long2IntOpenHashMap())
            }
        } else continue

        connections = connections or (1 shl dir.id)
        neighborCount++
    }
    network.nodes[pos.asLong()] = NetworkNode(pos, PipeConnections(connections), neighborCount, Long2IntOpenHashMap())
}

fun calculateDistances(network: PipeNetwork<*>) {
    val mutable = BlockPos.Mutable()
    network.nodes.forEach { (pos, node) ->
        if (node.isCorner()) {
            node.distances.defaultReturnValue(99999)
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
    dist.defaultReturnValue(99999)
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
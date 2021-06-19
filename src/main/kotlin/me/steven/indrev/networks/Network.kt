package me.steven.indrev.networks

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.networks.client.ClientNetworkInfo
import me.steven.indrev.networks.client.ClientServoNetworkInfo
import me.steven.indrev.networks.client.node.ClientServoNodeInfo
import me.steven.indrev.networks.energy.EnergyNetwork
import me.steven.indrev.networks.factory.ENERGY_NET_FACTORY
import me.steven.indrev.networks.factory.FLUID_NET_FACTORY
import me.steven.indrev.networks.factory.ITEM_NET_FACTORY
import me.steven.indrev.networks.factory.NetworkFactory
import me.steven.indrev.networks.fluid.FluidNetwork
import me.steven.indrev.networks.fluid.FluidNetworkState
import me.steven.indrev.networks.item.ItemNetwork
import me.steven.indrev.networks.item.ItemNetworkState
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*

abstract class Network(
    val type: Type<*>,
    val world: ServerWorld,
    val pipes: MutableSet<BlockPos> = hashSetOf(),
    val containers: MutableMap<BlockPos, EnumSet<Direction>> = hashMapOf()
) {

    protected val queue = hashMapOf<BlockPos, PriorityQueue<Node>>()

    protected fun buildQueue() {
        queue.clear()
        containers.forEach { (pos, _) ->
            find(pos, pos, 0, LongOpenHashSet())
        }
    }

    protected fun find(source: BlockPos, blockPos: BlockPos, count: Short, s: LongOpenHashSet) {
        DIRECTIONS.forEach { dir ->
            val offset = blockPos.offset(dir.opposite)
            if (pipes.contains(offset) && s.add(offset.asLong())) {
                find(source, offset, (count + 1).toShort(), s)
            }
            if (source != offset && containers.contains(offset) && containers[offset]!!.contains(dir)) {
                queue.computeIfAbsent(source) { PriorityQueue(containers.size) }.add(Node(source, offset, count, dir))
            }
        }
    }

    abstract fun tick(world: ServerWorld)

    open fun remove() {
        type.remove(world, this)
    }

    @Suppress("UNCHECKED_CAST")
    open fun appendPipe(block: Block, blockPos: BlockPos) {
        pipes.add(blockPos)
        type[blockPos] = this
    }

    open fun appendContainer(blockPos: BlockPos, direction: Direction) {
        containers.computeIfAbsent(blockPos) { EnumSet.noneOf(Direction::class.java) }.add(direction)
        type[blockPos] = this
    }

    companion object {

        val DIRECTIONS = Direction.values()

        fun <T : Network> handleBreak(type: Type<T>, pos: BlockPos) {
            type.networksByPos[pos.asLong()]?.remove()
            DIRECTIONS.forEach {
                val offset = pos.offset(it)
                handleUpdate(type, offset)
            }
        }

        fun <T : Network> handleUpdate(type: Type<T>, pos: BlockPos) {
            type.networksByPos[pos.asLong()]?.remove()
            type.queueUpdate(pos.asLong(), true)
        }
    }
    abstract class Type<T : Network>(val key: String) {

        abstract val factory: NetworkFactory<T>

        val states: WeakHashMap<World, NetworkState<T>> = WeakHashMap()

        val networksByPos = Long2ObjectOpenHashMap<T>()
        val networks = ObjectOpenHashSet<Network>()

        val queuedUpdates = LongOpenHashSet()
        val updatedPositions = LongOpenHashSet()

        var version = 0

        abstract fun createEmpty(world: ServerWorld): T

        abstract fun getNetworkState(world: ServerWorld): NetworkState<T>?

        abstract fun createClientNetworkInfo(world: ServerWorld): ClientNetworkInfo<*>?

        fun queueUpdate(pos: Long, force: Boolean = false) {
            if (!networksByPos.contains(pos) || force) queuedUpdates.add(pos)
        }

        operator fun set(pos: BlockPos, network: Network) {
            networksByPos[pos.asLong()] = network as T
        }

        fun remove(world: ServerWorld, network: Network) {
            network.pipes.forEach { pos ->
                networksByPos.remove(pos.asLong())
                getNetworkState(world)?.onRemoved(pos)
            }
            network.containers.forEach { (pos, _) ->
                networksByPos.remove(pos.asLong())
                getNetworkState(world)?.onRemoved(pos)
            }
            networks.remove(network)
        }

        fun tick(world: ServerWorld) {
            val state = getNetworkState(world)

            LongOpenHashSet(queuedUpdates).forEach { pos ->
                if (!updatedPositions.contains(pos)) {
                    val network = factory.deepScan(this, world, BlockPos.fromLong(pos))
                    if (network.pipes.isNotEmpty())
                        networks.add(network)
                    else
                        remove(world, network)
                }
            }
            queuedUpdates.clear()
            updatedPositions.clear()
            world.profiler.push("indrev_${key.lowercase()}NetworkTick")
            networks.forEach { network -> network.tick(world) }
            world.profiler.pop()

            (state as? ServoNetworkState<*>)?.sync(world)
            (state as? ServoNetworkState<*>)?.clearCachedData(false)
        }

        fun clear() {
            this.states.clear()
            this.queuedUpdates.clear()
            this.updatedPositions.clear()
            this.networksByPos.clear()
            this.networks.clear()
        }

        companion object {
            val ENERGY = object : Type<EnergyNetwork>(NetworkState.ENERGY_KEY) {

                override val factory: NetworkFactory<EnergyNetwork> = ENERGY_NET_FACTORY

                override fun createEmpty(world: ServerWorld): EnergyNetwork = EnergyNetwork(world)

                override fun getNetworkState(world: ServerWorld): NetworkState<EnergyNetwork>? {
                    return null
                }

                override fun createClientNetworkInfo(world: ServerWorld): ClientNetworkInfo<*>? {
                    return null
                }
            }
            val FLUID = object : Type<FluidNetwork>(NetworkState.FLUID_KEY) {

                override val factory: NetworkFactory<FluidNetwork> = FLUID_NET_FACTORY

                override fun createEmpty(world: ServerWorld): FluidNetwork = FluidNetwork(world)

                override fun getNetworkState(world: ServerWorld): FluidNetworkState {
                    return states.computeIfAbsent(world) { world.persistentStateManager.getOrCreate({ ServoNetworkState.readNbt(it) { FluidNetworkState(world) } }, { FluidNetworkState(world) }, key) } as FluidNetworkState
                }

                override fun createClientNetworkInfo(world: ServerWorld): ClientNetworkInfo<*> {
                    val state = getNetworkState(world)
                    return ClientServoNetworkInfo().also {
                        state.endpointData.forEach { (pos, data) ->
                            val info = ClientServoNodeInfo(pos, Object2ObjectOpenHashMap())
                            data.forEach { (dir, endpointData) ->
                                info.servos[dir] = endpointData.type
                            }
                            it.pipes[pos] = info
                        }
                    }
                }
            }
            val ITEM = object : Type<ItemNetwork>(NetworkState.ITEM_KEY) {

                override val factory: NetworkFactory<ItemNetwork> = ITEM_NET_FACTORY

                override fun createEmpty(world: ServerWorld): ItemNetwork = ItemNetwork(world)

                override fun getNetworkState(world: ServerWorld): ItemNetworkState {
                    return states.computeIfAbsent(world) { world.persistentStateManager.getOrCreate({ ItemNetworkState.readNbt(it) { ItemNetworkState(world) } },{ ItemNetworkState(world) }, key) } as ItemNetworkState
                }

                override fun createClientNetworkInfo(world: ServerWorld): ClientNetworkInfo<*> {
                    val state = getNetworkState(world)
                    return ClientServoNetworkInfo().also {
                        state.endpointData.forEach { (pos, data) ->
                            val info = ClientServoNodeInfo(pos, Object2ObjectOpenHashMap())
                            data.forEach { (dir, endpointData) ->
                                info.servos[dir] = endpointData.type
                            }
                            it.pipes[pos] = info
                        }
                    }
                }
            }

            fun valueOf(string: String): Type<*> {
                return when (string) {
                    NetworkState.ENERGY_KEY -> ENERGY
                    NetworkState.FLUID_KEY -> FLUID
                    NetworkState.ITEM_KEY -> ITEM
                    else -> throw IllegalArgumentException("Unknown network type $string")
                }
            }
        }
    }
}
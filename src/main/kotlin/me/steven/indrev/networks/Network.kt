package me.steven.indrev.networks

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
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
import me.steven.indrev.utils.energyNetworkState
import me.steven.indrev.utils.fluidNetworkState
import me.steven.indrev.utils.itemNetworkState
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*

abstract class Network(
    val type: Type<*>,
    val world: ServerWorld,
    val pipes: MutableSet<BlockPos> = ObjectOpenHashSet(),
    val containers: MutableMap<BlockPos, EnumSet<Direction>> = Object2ObjectOpenHashMap()
) {

    val state = type.getNetworkState(world)

    protected val queue = Object2ObjectOpenHashMap<BlockPos, MutableList<Node>>(containers.size)

    protected fun isQueueValid(): Boolean {
        if (queue.isEmpty() && containers.isNotEmpty()) {
            buildQueue()
        }
        return queue.isNotEmpty()
    }

    protected fun buildQueue() {
        queue.clear()
        containers.forEach { (pos, _) ->
            find(pos, pos, 0, LongOpenHashSet())
        }
    }

    protected fun find(source: BlockPos, blockPos: BlockPos, count: Int, s: LongOpenHashSet) {
        DIRECTIONS.forEach { dir ->
            val offset = blockPos.offset(dir.opposite)
            if (pipes.contains(offset) && s.add(offset.asLong())) {
                find(source, offset, count + 1, s)
            }
            if (source != offset && containers.contains(offset) && containers[offset]!!.contains(dir)) {
                queue.computeIfAbsent(source, Object2ObjectFunction { ArrayList(containers.size) }).add(Node(source, offset, count, dir))
            }
        }
    }

    abstract fun tick(world: ServerWorld)

    open fun remove() {
        state.remove(this)
    }

    @Suppress("UNCHECKED_CAST")
    open fun appendPipe(block: Block, blockPos: BlockPos) {
        pipes.add(blockPos)

        state[blockPos] = this
    }

    open fun appendContainer(blockPos: BlockPos, direction: Direction) {
        containers.computeIfAbsent(blockPos) { EnumSet.noneOf(Direction::class.java) }.add(direction)
        state[blockPos] = this
    }

    companion object {

        val DIRECTIONS = Direction.values()

        fun <T : Network> handleBreak(state: NetworkState<T>, pos: BlockPos) {
            state.networksByPos[pos.asLong()]?.remove()
            DIRECTIONS.forEach {
                val offset = pos.offset(it)
                handleUpdate(state, offset)
            }
        }

        fun <T : Network> handleUpdate(state: NetworkState<T>, pos: BlockPos) {
            state.networksByPos[pos.asLong()]?.remove()
            state.queueUpdate(pos.asLong(), true)
        }
    }
    abstract class Type<T : Network>(val key: String) {

        abstract val factory: NetworkFactory<T>

        abstract fun createEmpty(world: ServerWorld): T

        abstract fun getNetworkState(world: ServerWorld): NetworkState<T>

        abstract fun createClientNetworkInfo(world: ServerWorld): ClientNetworkInfo<*>?

        companion object {
            val ENERGY = object : Type<EnergyNetwork>(NetworkState.ENERGY_KEY) {

                override val factory: NetworkFactory<EnergyNetwork> = ENERGY_NET_FACTORY

                override fun createEmpty(world: ServerWorld): EnergyNetwork = EnergyNetwork(world)

                override fun getNetworkState(world: ServerWorld): NetworkState<EnergyNetwork> = world.energyNetworkState

                override fun createClientNetworkInfo(world: ServerWorld): ClientNetworkInfo<*>? {
                    return null
                }
            }
            val FLUID = object : Type<FluidNetwork>(NetworkState.FLUID_KEY) {

                override val factory: NetworkFactory<FluidNetwork> = FLUID_NET_FACTORY

                override fun createEmpty(world: ServerWorld): FluidNetwork = FluidNetwork(world)

                override fun getNetworkState(world: ServerWorld): FluidNetworkState = world.fluidNetworkState

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

                override fun getNetworkState(world: ServerWorld): ItemNetworkState = world.itemNetworkState

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
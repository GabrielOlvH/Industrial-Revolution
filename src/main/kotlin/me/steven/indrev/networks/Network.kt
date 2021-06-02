package me.steven.indrev.networks

import alexiil.mc.lib.attributes.fluid.impl.EmptyGroupedFluidInv
import alexiil.mc.lib.attributes.item.impl.EmptyGroupedItemInv
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.blocks.machine.pipes.ItemPipeBlock
import me.steven.indrev.networks.energy.EnergyNetwork
import me.steven.indrev.networks.fluid.FluidNetwork
import me.steven.indrev.networks.fluid.FluidNetworkState
import me.steven.indrev.networks.item.ItemNetwork
import me.steven.indrev.networks.item.ItemNetworkState
import me.steven.indrev.utils.energyOf
import me.steven.indrev.utils.groupedFluidInv
import me.steven.indrev.utils.groupedItemInv
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtLong
import net.minecraft.nbt.NbtString
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
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
            val offset = blockPos.offset(dir)
            if (pipes.contains(offset) && s.add(offset.asLong())) {
                find(source, offset, (count + 1).toShort(), s)
            }
            if (source != offset && containers.contains(offset) && containers[offset]!!.contains(dir.opposite)) {
                queue.computeIfAbsent(source) { PriorityQueue(containers.size) }.add(Node(source, offset, count, dir.opposite))
            }
        }
    }

    abstract fun tick(world: ServerWorld)

    open fun writeNbt(tag: NbtCompound): NbtCompound {
        writePositions(tag)
        return tag
    }

    open fun readNbt(world: ServerWorld, tag: NbtCompound) {
        readPositions(tag)
    }

    open fun remove() {
        val state = type.getNetworkState(world)
        state.networks.remove(this)
        pipes.forEach { state.remove(it) }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <V : Network> appendPipe(state: NetworkState<V>, block: Block, blockPos: BlockPos) {
        pipes.add(blockPos)
        state[blockPos] = this as V
    }

    open fun appendContainer(blockPos: BlockPos, direction: Direction) {
        containers.computeIfAbsent(blockPos) { EnumSet.noneOf(Direction::class.java) }.add(direction)
    }

    fun writePositions(tag: NbtCompound) {
        val pipesList = NbtList()
        pipes.forEach { pos ->
            pipesList.add(NbtLong.of(pos.asLong()))
        }
        val containersList = NbtList()
        containers.forEach { (pos, directions) ->
            val machineTag = NbtCompound()
            machineTag.putLong("pos", pos.asLong())
            val dirList = NbtList()
            directions.forEach { dir ->
                dirList.add(NbtString.of(dir.toString()))
            }
            machineTag.put("dir", dirList)
            containersList.add(machineTag)
        }
        tag.put("cables", pipesList)
        tag.put("machines", containersList)
    }

    fun readPositions(tag: NbtCompound) {
        val pipesList = tag.getList("cables", 4)
        val containersList = tag.getList("machines", 10)
        pipesList.forEach { cableTag ->
            cableTag as NbtLong
            this.pipes.add(BlockPos.fromLong(cableTag.longValue()).toImmutable())
        }
        containersList.forEach { machineTag ->
            machineTag as NbtCompound
            val posLong = machineTag.getLong("pos")
            val pos = BlockPos.fromLong(posLong)
            val dirList = machineTag.getList("dir", 8)
            dirList.forEach { dirTag ->
                dirTag as NbtString
                val dir = Direction.valueOf(dirTag.asString().uppercase())
                appendContainer(pos, dir)
            }
        }
    }

    companion object {

        val DIRECTIONS = Direction.values()

        fun <T : Network> handleBreak(type: Type<T>, world: ServerWorld, pos: BlockPos) {
            val state = type.getNetworkState(world)
            if (state.contains(pos))
                state[pos]?.remove()
            DIRECTIONS.forEach {
                val offset = pos.offset(it)
                handleUpdate(type, world, offset)
            }
        }

        fun <T : Network> handleUpdate(type: Type<T>, world: ServerWorld, pos: BlockPos) {
            val state = type.getNetworkState(world)
            if (state.contains(pos))
                state[pos]?.remove()
            val network = type.createEmpty(world)
            state.networks.add(network)
            val scanned = hashSetOf<BlockPos>()
            DIRECTIONS.forEach { dir ->
                buildNetwork(scanned, state, network, world.getChunk(pos), world, pos, pos, dir)
            }
            if (network.containers.isEmpty() || network.pipes.isEmpty())
                network.remove()
            state.markDirty()
        }

        private fun <T : Network> buildNetwork(
            scanned: MutableSet<BlockPos>,
            state: NetworkState<T>,
            network: Network,
            chunk: Chunk,
            world: ServerWorld,
            blockPos: BlockPos,
            source: BlockPos,
            direction: Direction
        ) {
            if (network.type.isContainer(world, blockPos, direction.opposite)) {
                network.appendContainer(blockPos, direction.opposite)
            }
            if (blockPos != source && !scanned.add(blockPos)) return
            val blockState = chunk.getBlockState(blockPos) ?: return
            val block = blockState.block
            if (network.type.isPipe(blockState)) {
                if (state.contains(blockPos)) {
                    val oldNetwork = state[blockPos]
                    if (state.networks.contains(oldNetwork) && oldNetwork != network) {
                        oldNetwork?.remove()
                    }
                }
                DIRECTIONS.forEach { dir ->
                    if (blockState[BasePipeBlock.getProperty(dir)]) {
                        val nPos = blockPos.offset(dir)
                        if (nPos.x shr 4 == chunk.pos.x && nPos.z shr 4 == chunk.pos.z)
                            buildNetwork(scanned, state, network, chunk, world, nPos, source, dir)
                        else
                            buildNetwork(scanned, state, network, world.getChunk(nPos), world, nPos, source, dir)
                    }
                }
                if (blockState[BasePipeBlock.getProperty(direction.opposite)])
                    network.appendPipe(state, block, blockPos.toImmutable())
            }
        }

        fun readNbt(world: ServerWorld, tag: NbtCompound): Network {
            val type = if (tag.contains("type")) Type.valueOf(tag.getString("type").uppercase(Locale.getDefault())) else Type.ENERGY
            val network = type.createEmpty(world)
            network.readPositions(tag)
            return network
        }
    }
    abstract class Type<T : Network>(val key: String) {

        val states: WeakHashMap<World, NetworkState<T>> = WeakHashMap()


        abstract fun createEmpty(world: ServerWorld): T

        abstract fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean

        abstract fun isPipe(blockState: BlockState): Boolean

        open fun getNetworkState(world: ServerWorld): NetworkState<T> {
            return states.computeIfAbsent(world) { world.persistentStateManager.getOrCreate({ NetworkState.readNbt(it) { NetworkState(this, world) } }, { NetworkState(this, world) }, key) }
        }

        companion object {
            val ENERGY = object : Type<EnergyNetwork>(NetworkState.ENERGY_KEY) {

                override fun createEmpty(world: ServerWorld): EnergyNetwork = EnergyNetwork(world)

                override fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean = energyOf(world, pos, direction.opposite) != null

                override fun isPipe(blockState: BlockState): Boolean = blockState.block is CableBlock
            }
            val FLUID = object : Type<FluidNetwork>(NetworkState.FLUID_KEY) {

                override fun createEmpty(world: ServerWorld): FluidNetwork = FluidNetwork(world)

                override fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean = groupedFluidInv(world, pos, direction.opposite) != EmptyGroupedFluidInv.INSTANCE

                override fun isPipe(blockState: BlockState): Boolean = blockState.block is FluidPipeBlock

                override fun getNetworkState(world: ServerWorld): FluidNetworkState {
                    return states.computeIfAbsent(world) { world.persistentStateManager.getOrCreate({ ServoNetworkState.readNbt(it) { FluidNetworkState(world) } }, { FluidNetworkState(world) }, key) } as FluidNetworkState
                }
            }
            val ITEM = object : Type<ItemNetwork>(NetworkState.ITEM_KEY) {

                override fun createEmpty(world: ServerWorld): ItemNetwork = ItemNetwork(world)

                override fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean = groupedItemInv(world, pos, direction.opposite) != EmptyGroupedItemInv.INSTANCE

                override fun isPipe(blockState: BlockState): Boolean = blockState.block is ItemPipeBlock

                override fun getNetworkState(world: ServerWorld): ItemNetworkState {
                    return states.computeIfAbsent(world) { world.persistentStateManager.getOrCreate({ ServoNetworkState.readNbt(it) { ItemNetworkState(world) } },{ ItemNetworkState(world) }, key) } as ItemNetworkState
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
package me.steven.indrev.networks

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.blocks.machine.CableBlock
import me.steven.indrev.networks.energy.EnergyNetwork
import me.steven.indrev.utils.energyOf
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.StringTag
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

    open fun toTag(tag: CompoundTag): CompoundTag {
        writePositions(tag)
        return tag
    }

    open fun fromTag(world: ServerWorld, tag: CompoundTag) {
        readPositions(tag)
    }

    fun remove() {
        val state = type.getNetworkState(world)
        state.networks.remove(this)
        pipes.forEach { state.networksByPos.remove(it) }
    }

    @Suppress("UNCHECKED_CAST")
    open fun <T : Network> appendPipe(state: NetworkState<T>, block: Block, blockPos: BlockPos) {
        pipes.add(blockPos)
        state.networksByPos[blockPos] = this as T
    }

    fun appendContainer(blockPos: BlockPos, direction: Direction) {
        containers.computeIfAbsent(blockPos) { EnumSet.noneOf(Direction::class.java) }.add(direction)
    }

    fun writePositions(tag: CompoundTag) {
        val pipesList = ListTag()
        pipes.forEach { pos ->
            pipesList.add(LongTag.of(pos.asLong()))
        }
        val containersList = ListTag()
        containers.forEach { (pos, directions) ->
            val machineTag = CompoundTag()
            machineTag.putLong("pos", pos.asLong())
            val dirList = ListTag()
            directions.forEach { dir ->
                dirList.add(StringTag.of(dir.toString()))
            }
            machineTag.put("dir", dirList)
            containersList.add(machineTag)
        }
        tag.put("pipes", pipesList)
        tag.put("containers", containersList)
    }

    fun readPositions(tag: CompoundTag) {
        val pipesList = tag.getList("pipes", 4)
        val containersList = tag.getList("containers", 10)
        pipesList.forEach { cableTag ->
            cableTag as LongTag
            this.pipes.add(BlockPos.fromLong(cableTag.long).toImmutable())
        }
        containersList.forEach { machineTag ->
            machineTag as CompoundTag
            val posLong = machineTag.getLong("pos")
            val pos = BlockPos.fromLong(posLong)
            val dirList = machineTag.getList("dir", 8)
            val directions = EnumSet.noneOf(Direction::class.java)
            dirList.forEach { dirTag ->
                dirTag as StringTag
                val dir = Direction.valueOf(dirTag.asString().toUpperCase())
                directions.add(dir)
            }
            this.containers[pos] = directions
        }
    }

    companion object {

        val DIRECTIONS = Direction.values()

        fun <T : Network> handleBreak(type: Type<T>, world: ServerWorld, pos: BlockPos) {
            val state = type.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
            DIRECTIONS.forEach {
                val offset = pos.offset(it)
                handleUpdate(type, world, offset)
            }
        }

        fun <T : Network> handleUpdate(type: Type<T>, world: ServerWorld, pos: BlockPos) {
            val state = type.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
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
                if (state.networksByPos.containsKey(blockPos)) {
                    val oldNetwork = state.networksByPos[blockPos]
                    if (state.networks.contains(oldNetwork) && oldNetwork != network) {
                        oldNetwork?.remove()
                    }
                }
                DIRECTIONS.forEach { dir ->
                    if (blockState[CableBlock.getProperty(dir)]) {
                        val nPos = blockPos.offset(dir)
                        if (nPos.x shr 4 == chunk.pos.x && nPos.z shr 4 == chunk.pos.z)
                            buildNetwork(scanned, state, network, chunk, world, nPos, source, dir)
                        else
                            buildNetwork(scanned, state, network, world.getChunk(nPos), world, nPos, source, dir)
                    }
                }
                if (blockState[CableBlock.getProperty(direction.opposite)])
                    network.appendPipe(state, block, blockPos.toImmutable())
            }
        }

        fun fromTag(world: ServerWorld, tag: CompoundTag): Network {
            val type = if (tag.contains("type")) Type.valueOf(tag.getString("type").toUpperCase()) else Type.ENERGY
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

        fun getNetworkState(world: ServerWorld): NetworkState<T> {
            return states.computeIfAbsent(world) { world.persistentStateManager.getOrCreate({ NetworkState(this, world, key) }, key) }
        }

        companion object {
            val ENERGY = object : Type<EnergyNetwork>(NetworkState.ENERGY_KEY) {

                override fun createEmpty(world: ServerWorld): EnergyNetwork = EnergyNetwork(world)

                override fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean = energyOf(world, pos, direction) != null

                override fun isPipe(blockState: BlockState): Boolean = blockState.block is CableBlock
            }
            val FLUID = object : Type<EnergyNetwork>(NetworkState.FLUID_KEY) {

                override fun createEmpty(world: ServerWorld): EnergyNetwork = throw NotImplementedError()

                override fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean = false

                override fun isPipe(blockState: BlockState): Boolean = false
            }
            val ITEM = object : Type<EnergyNetwork>(NetworkState.ITEM_KEY) {

                override fun createEmpty(world: ServerWorld): EnergyNetwork = throw NotImplementedError()

                override fun isContainer(world: ServerWorld, pos: BlockPos, direction: Direction): Boolean = false

                override fun isPipe(blockState: BlockState): Boolean = false
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
package me.steven.indrev.energy

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.CableBlock
import me.steven.indrev.utils.energyOf
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.StringTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.chunk.Chunk
import java.util.*
import kotlin.math.absoluteValue

class EnergyNetwork(
    val world: ServerWorld,
    val cables: MutableSet<BlockPos> = hashSetOf(),
    val machines: MutableMap<BlockPos, EnumSet<Direction>> = hashMapOf()
) {

    var tier = Tier.MK1
    private val maxCableTransfer: Double
        get() = when (tier) {
            Tier.MK1 -> IndustrialRevolution.CONFIG.cables.cableMk1.maxInput
            Tier.MK2 -> IndustrialRevolution.CONFIG.cables.cableMk2.maxInput
            Tier.MK3 -> IndustrialRevolution.CONFIG.cables.cableMk3.maxInput
            else -> IndustrialRevolution.CONFIG.cables.cableMk4.maxInput
        }

    private val queue = hashMapOf<BlockPos, PriorityQueue<Node>>()

    private fun buildQueue() {
        queue.clear()
        machines.forEach { (pos, _) ->
            find(pos, pos, 0, LongOpenHashSet())
        }
    }

    private fun find(source: BlockPos, blockPos: BlockPos, count: Short, s: LongOpenHashSet) {
        DIRECTIONS.forEach { dir ->
            val offset = blockPos.offset(dir)
            if (cables.contains(offset) && s.add(offset.asLong())) {
                find(source, offset, (count + 1).toShort(), s)
            }
            if (source != offset && machines.contains(offset) && machines[offset]!!.contains(dir.opposite)) {
                queue.computeIfAbsent(source) { PriorityQueue(machines.size) }.add(Node(source, offset, count, dir.opposite))
            }
        }
    }

    fun tick(world: ServerWorld) {
        if (machines.isEmpty()) return
        else if (queue.isEmpty()) {
            buildQueue()
        }
        if (queue.isNotEmpty()) {
            val remainingInputs = Object2DoubleOpenHashMap<BlockPos>()
            machines.forEach { (pos, directions) ->
                val q = PriorityQueue(queue[pos] ?: return@forEach)
                directions.forEach inner@{ dir ->
                    val energyIo = energyOf(world, pos, dir) ?: return@inner
                    var remaining = energyIo.maxOutput

                    while (q.isNotEmpty() && energyIo.supportsExtraction() && remaining > 1e-9) {
                        val (_, targetPos, _, targetDir) = q.poll()
                        val target = energyOf(world, targetPos, targetDir) ?: continue
                        val maxInput = remainingInputs.computeIfAbsent(targetPos) { target.maxInput }
                        if (maxInput < 1e-9) continue

                        val amount = remaining.coerceAtMost(maxInput).coerceAtMost(maxCableTransfer)
                        val before = target.energy
                        val moved = EnergyMovement.move(energyIo, target, amount)
                        val after = target.energy
                        if (moved > 1e-9 && (after - before).absoluteValue > 1e-9) {
                            remaining -= moved
                            remainingInputs.addTo(targetPos, -moved)
                        }
                    }
                }
            }
        }
    }

    fun remove() {
        val state = EnergyNetworkState.getNetworkState(world)
        state.networks.remove(this)
        cables.forEach { state.networksByPos.remove(it) }
    }

    fun appendCable(state: EnergyNetworkState, block: CableBlock, blockPos: BlockPos) {
        tier = block.tier
        cables.add(blockPos)
        state.networksByPos[blockPos] = this
    }

    fun appendMachine(blockPos: BlockPos, direction: Direction) {
        machines.computeIfAbsent(blockPos) { EnumSet.noneOf(Direction::class.java) }.add(direction)
    }

    fun toTag(tag: CompoundTag) {
        val cablesList = ListTag()
        cables.forEach { pos ->
            cablesList.add(LongTag.of(pos.asLong()))
        }
        val machinesList = ListTag()
        machines.forEach { (pos, directions) ->
            val machineTag = CompoundTag()
            machineTag.putLong("pos", pos.asLong())
            val dirList = ListTag()
            directions.forEach { dir ->
                dirList.add(StringTag.of(dir.toString()))
            }
            machineTag.put("dir", dirList)
            machinesList.add(machineTag)
        }
        tag.put("cables", cablesList)
        tag.put("machines", machinesList)
        tag.putInt("tier", tier.ordinal)
    }

    companion object {
        
        val DIRECTIONS = Direction.values()

        private const val MAX_VALUE = (Integer.MAX_VALUE - 1).toDouble()

        private val EnergyIo.maxInput: Double
            get() = MAX_VALUE - insert(MAX_VALUE, Simulation.SIMULATE)
        private val EnergyIo.maxOutput: Double
            get() = extract(MAX_VALUE, Simulation.SIMULATE)

        fun handleBreak(world: ServerWorld, pos: BlockPos) {
            val state = EnergyNetworkState.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
            DIRECTIONS.forEach {
                val offset = pos.offset(it)
                handleUpdate(world, offset)
            }
        }

        fun handleUpdate(world: ServerWorld, pos: BlockPos) {
            val state = EnergyNetworkState.getNetworkState(world)
            if (state.networksByPos.containsKey(pos))
                state.networksByPos[pos]?.remove()
            val network = EnergyNetwork(world)
            state.networks.add(network)
            val scanned = hashSetOf<BlockPos>()
            DIRECTIONS.forEach { dir ->
                buildNetwork(scanned, state, network, world.getChunk(pos), world, pos, pos, dir)
            }
            if (network.machines.isEmpty() || network.cables.isEmpty())
                network.remove()
            state.markDirty()
        }

        private fun buildNetwork(scanned: MutableSet<BlockPos>, state: EnergyNetworkState, network: EnergyNetwork, chunk: Chunk, world: ServerWorld, blockPos: BlockPos, source: BlockPos, direction: Direction) {
            if (energyOf(world, blockPos, direction.opposite) != null) {
                network.appendMachine(blockPos, direction.opposite)
            }
            if (blockPos != source && !scanned.add(blockPos)) return
            val blockState = chunk.getBlockState(blockPos) ?: return
            val block = blockState.block
            if (block is CableBlock) {
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
                    network.appendCable(state, block, blockPos.toImmutable())
            }
        }

        fun fromTag(world: ServerWorld, tag: CompoundTag): EnergyNetwork {
            val cablesList = tag.getList("cables", 4)
            val machinesList = tag.getList("machines", 10)
            val network = EnergyNetwork(world)
            cablesList.forEach { cableTag ->
                cableTag as LongTag
                network.cables.add(BlockPos.fromLong(cableTag.long).toImmutable())
            }
            machinesList.forEach { machineTag ->
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
                network.machines[pos] = directions
            }
            val tier = Tier.values()[tag.getInt("tier")]
            network.tier = tier
            return network
        }
    }
}
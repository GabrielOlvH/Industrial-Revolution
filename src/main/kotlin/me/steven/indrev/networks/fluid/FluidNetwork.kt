package me.steven.indrev.networks.fluid

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.NetworkState
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.fluidExtractableOf
import me.steven.indrev.utils.fluidInsertableOf
import me.steven.indrev.utils.minus
import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.hashMapOf
import kotlin.collections.hashSetOf
import kotlin.collections.isNotEmpty

class FluidNetwork(
    world: ServerWorld,
    pipes: MutableSet<BlockPos> = hashSetOf(),
    containers: MutableMap<BlockPos, EnumSet<Direction>> = hashMapOf()
) : Network(Type.FLUID, world, pipes, containers) {

    var tier = Tier.MK1
    private val maxCableTransfer: FluidAmount
        get() = FluidAmount.ofWhole(when (tier) {
            Tier.MK1 -> IRConfig.cables.fluidPipeMk1
            Tier.MK2 -> IRConfig.cables.fluidPipeMk2
            Tier.MK3 -> IRConfig.cables.fluidPipeMk3
            else -> IRConfig.cables.fluidPipeMk4
        }.toLong())

    var lastTransferred: FluidKey? = null

    override fun tick(world: ServerWorld) {
        if (world.time % 20 != 0L) return
        val state = Type.FLUID.getNetworkState(world) as FluidNetworkState
        if (containers.isEmpty()) return
        else if (queue.isEmpty())
            buildQueue()
        if (queue.isNotEmpty()) {
            containers.forEach { (pos, directions) ->
                val originalQueue = queue[pos] ?: return@forEach

                val sortedQueues = hashMapOf<EndpointData.Mode, PriorityQueue<Node>>()

                directions.forEach inner@{ dir ->
                    val data = state.getEndpointData(pos.offset(dir), dir.opposite) ?: return@inner

                    val filter = lastTransferred?.exactFilter ?: FluidFilter { true }
                    val queue =
                        PriorityQueue(sortedQueues.computeIfAbsent(data.mode!!) {
                            if (data.mode == EndpointData.Mode.NEAREST_FIRST)
                                PriorityQueue(originalQueue)
                            else
                                PriorityQueue(data.mode!!.getFluidComparator(world, data.type, filter)).also { q -> q.addAll(originalQueue) }
                        })

                    if (data.type == EndpointData.Type.OUTPUT)
                        tickOutput(pos, dir, queue, state, filter)
                    else if (data.type == EndpointData.Type.RETRIEVER)
                        tickRetriever(pos, dir, queue, state, filter)
                }
            }
        }
        lastTransferred = null
    }

    private fun tickOutput(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: FluidNetworkState, fluidFilter: FluidFilter) {
        val extractable = fluidExtractableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining.asInexactDouble() > 1e-9) {
            val (_, targetPos, _, targetDir) = queue.poll()
            val targetData = state.getEndpointData(targetPos.offset(targetDir), targetDir.opposite)
            val input = targetData == null || targetData.type == EndpointData.Type.INPUT
            if (!input) continue

            val insertable = fluidInsertableOf(world, targetPos, targetDir.opposite)
            val moved = FluidVolumeUtil.move(extractable, insertable, fluidFilter, remaining, Simulation.ACTION)
            if (!moved.isEmpty)
                lastTransferred = moved.fluidKey
            remaining -= moved.amount()
        }
    }

    private fun tickRetriever(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: FluidNetworkState, fluidFilter: FluidFilter) {
        val insertable = fluidInsertableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining.asInexactDouble() > 1e-9) {
            val (_, targetPos, _, targetDir) = queue.poll()
            val targetData = state.getEndpointData(targetPos.offset(targetDir), targetDir.opposite)
            val isRetriever = targetData?.type == EndpointData.Type.RETRIEVER
            if (isRetriever) continue

            val extractable = fluidExtractableOf(world, targetPos, targetDir.opposite)
            val moved = FluidVolumeUtil.move(extractable, insertable, fluidFilter, remaining, Simulation.ACTION)
            if (!moved.isEmpty)
                lastTransferred = moved.fluidKey
            remaining -= moved.amount()
        }
    }

    override fun <T : Network> appendPipe(state: NetworkState<T>, block: Block, blockPos: BlockPos) {
        val cable = block as? FluidPipeBlock ?: return
        this.tier = cable.tier
        super.appendPipe(state, block, blockPos)
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        super.toTag(tag)
        tag.putInt("tier", tier.ordinal)
        return tag
    }

    override fun fromTag(world: ServerWorld, tag: CompoundTag) {
        super.fromTag(world, tag)
        val tier = Tier.values()[tag.getInt("tier")]
        this.tier = tier
    }

    companion object {

        fun fromTag(world: ServerWorld, tag: CompoundTag): FluidNetwork {
            val network = Network.fromTag(world, tag) as FluidNetwork
            val tier = Tier.values()[tag.getInt("tier")]
            network.tier = tier
            return network
        }
    }
}
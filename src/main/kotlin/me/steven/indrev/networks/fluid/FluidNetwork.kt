package me.steven.indrev.networks.fluid

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.*
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*
import kotlin.collections.component1
import kotlin.collections.component2

class FluidNetwork(
    world: ServerWorld,
    pipes: MutableSet<BlockPos> = ObjectOpenHashSet(),
    containers: MutableMap<BlockPos, EnumSet<Direction>> = Object2ObjectOpenHashMap()
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

    private val deques = Object2ObjectOpenHashMap<BlockPos, EnumMap<EndpointData.Mode, ReusableArrayDeque<Node>>>()

    override fun tick(world: ServerWorld) {
        if (world.time % 20 != 0L) return
        val state = Type.FLUID.getNetworkState(world) as FluidNetworkState
        if (isQueueValid()) {
            containers.forEach { (pos, directions) ->
                if (!world.isLoaded(pos)) return@forEach

                val nodes = queue[pos] ?: return@forEach

                directions.forEach inner@{ dir ->
                    val data = state.getEndpointData(pos.offset(dir), dir.opposite) ?: return@inner

                    val filter = lastTransferred?.exactFilter ?: NO_FLUID_FILTER

                    val deque = getQueue(pos, data, filter, nodes)

                    if (data.type == EndpointData.Type.OUTPUT)
                        tickOutput(pos, dir, deque, state, filter)
                    else if (data.type == EndpointData.Type.RETRIEVER)
                        tickRetriever(pos, dir, deque, state, filter)

                    deque.resetHead()
                }
            }
        }
        lastTransferred = null
    }

    private fun getQueue(pos: BlockPos, data: EndpointData, filter: FluidFilter, nodes: List<Node>): ReusableArrayDeque<Node> {
        var queuesByNodes = deques[pos]
        if (queuesByNodes == null) {
            queuesByNodes = EnumMap(EndpointData.Mode::class.java)
            this.deques[pos] = queuesByNodes
        }
        var queue = queuesByNodes[data.mode]
        if (queue == null) {
            queue = ReusableArrayDeque(nodes)
            queue.apply(data.mode!!.getFluidSorter(world, data.type) { filter.matches(it) })
            queuesByNodes[data.mode] = queue
        }
        if (data.mode == EndpointData.Mode.ROUND_ROBIN || data.mode == EndpointData.Mode.RANDOM) {
            queue.apply(data.mode!!.getFluidSorter(world, data.type) { filter.matches(it) })
        }

        return queue
    }

    private fun tickOutput(pos: BlockPos, dir: Direction, queue: ReusableArrayDeque<Node>, state: FluidNetworkState, fluidFilter: FluidFilter) {
        val extractable = fluidExtractableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining.asInexactDouble() > 1e-9) {
            val (_, targetPos, _, targetDir) = queue.removeFirst()
            if (!world.isLoaded(targetPos)) continue
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

    private fun tickRetriever(pos: BlockPos, dir: Direction, queue: ReusableArrayDeque<Node>, state: FluidNetworkState, fluidFilter: FluidFilter) {
        val insertable = fluidInsertableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining.asInexactDouble() > 1e-9) {
            val (_, targetPos, _, targetDir) = queue.removeFirst()
            if (!world.isLoaded(targetPos)) continue
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

    override fun appendPipe(block: Block, blockPos: BlockPos) {
        val cable = block as? FluidPipeBlock ?: return
        this.tier = cable.tier
        super.appendPipe(block, blockPos)
    }

    companion object {
        private val NO_FLUID_FILTER = FluidFilter { true }
    }
}
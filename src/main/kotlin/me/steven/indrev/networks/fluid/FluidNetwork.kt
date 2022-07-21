package me.steven.indrev.networks.fluid

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.ReusableArrayDeque
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.fluidStorageOf
import me.steven.indrev.utils.isLoaded
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
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

    private val maxCableTransfer: Long
        get() = when (tier) {
            Tier.MK1 -> IRConfig.cables.fluidPipeMk1
            Tier.MK2 -> IRConfig.cables.fluidPipeMk2
            Tier.MK3 -> IRConfig.cables.fluidPipeMk3
            else -> IRConfig.cables.fluidPipeMk4
        }.toLong() * bucket

    var lastTransferred: FluidVariant = FluidVariant.blank()

    private val deques = Object2ObjectOpenHashMap<BlockPos, EnumMap<EndpointData.Mode, ReusableArrayDeque<Node>>>()

    private var ticks = 0

    override fun tick(world: ServerWorld) {
        ticks++
        if (ticks % 20 != 0) return
        val state = Type.FLUID.getNetworkState(world) as FluidNetworkState
        if (isQueueValid()) {
            containers.forEach { (pos, directions) ->
                if (!world.isLoaded(pos)) return@forEach

                val nodes = queue[pos] ?: return@forEach

                directions.forEach inner@{ dir ->
                    val data = state.getEndpointData(pos.offset(dir), dir.opposite) ?: return@inner

                    val filter: (FluidVariant) -> Boolean = { v -> !lastTransferred.isBlank && v == lastTransferred }

                    val deque = getQueue(pos, data, filter, nodes)

                    if (data.type == EndpointData.Type.OUTPUT)
                        tickOutput(pos, dir, deque, state, filter)
                    else if (data.type == EndpointData.Type.RETRIEVER)
                        tickRetriever(pos, dir, deque, state, filter)

                    deque.resetHead()
                }
            }
        }
        lastTransferred = FluidVariant.blank()
    }

    private fun getQueue(pos: BlockPos, data: EndpointData, filter: (FluidVariant) -> Boolean, nodes: List<Node>): ReusableArrayDeque<Node> {
        var queuesByNodes = deques[pos]
        if (queuesByNodes == null) {
            queuesByNodes = EnumMap(EndpointData.Mode::class.java)
            this.deques[pos] = queuesByNodes
        }
        var queue = queuesByNodes[data.mode]
        if (queue == null) {
            queue = ReusableArrayDeque(nodes)
            queue.apply(data.mode!!.getFluidSorter(world, data.type) { filter(it) })
            queuesByNodes[data.mode] = queue
        }
        if (data.mode == EndpointData.Mode.ROUND_ROBIN || data.mode == EndpointData.Mode.RANDOM) {
            queue.apply(data.mode!!.getFluidSorter(world, data.type) { filter(it) })
        }

        return queue
    }

    private fun tickOutput(pos: BlockPos, dir: Direction, queue: ReusableArrayDeque<Node>, state: FluidNetworkState, fluidFilter: (FluidVariant) -> Boolean) {
        val extractable = fluidStorageOf(world, pos, dir)
        var remaining = maxCableTransfer
        updateLastTransferred(extractable)
        while (queue.isNotEmpty() && remaining > 0) {
            val (_, targetPos, _, targetDir) = queue.removeFirst()
            if (!world.isLoaded(targetPos)) continue
            val targetData = state.getEndpointData(targetPos.offset(targetDir), targetDir.opposite)
            val input = targetData == null || targetData.type == EndpointData.Type.INPUT
            if (!input) continue

            val insertable = fluidStorageOf(world, targetPos, targetDir)
            val moved = StorageUtil.move(extractable, insertable, fluidFilter, remaining, null)
            remaining -= moved
        }
    }

    private fun tickRetriever(pos: BlockPos, dir: Direction, queue: ReusableArrayDeque<Node>, state: FluidNetworkState, fluidFilter: (FluidVariant) -> Boolean) {
        val insertable = fluidStorageOf(world, pos, dir)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining > 0) {
            val (_, targetPos, _, targetDir) = queue.removeFirst()
            if (!world.isLoaded(targetPos)) continue
            val targetData = state.getEndpointData(targetPos.offset(targetDir), targetDir.opposite)
            val isRetriever = targetData?.type == EndpointData.Type.RETRIEVER
            if (isRetriever) continue

            val extractable = fluidStorageOf(world, targetPos, targetDir)
            updateLastTransferred(extractable)
            val moved = StorageUtil.move(extractable, insertable, fluidFilter, remaining, null)
            remaining -= moved
        }
    }

    private fun updateLastTransferred(extractable: Storage<FluidVariant>?) {
        if (lastTransferred.isBlank) {
            val content = StorageUtil.findExtractableContent(extractable, null)
            if (content != null) {
                lastTransferred = content.resource
            }
        }
    }

    override fun appendPipe(block: Block, blockPos: BlockPos) {
        val cable = block as? FluidPipeBlock ?: return
        this.tier = cable.tier
        super.appendPipe(block, blockPos)
    }
}
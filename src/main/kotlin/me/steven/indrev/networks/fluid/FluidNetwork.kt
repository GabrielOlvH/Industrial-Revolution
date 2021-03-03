package me.steven.indrev.networks.fluid

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.NetworkState
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.*
import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*

class FluidNetwork(
    world: ServerWorld,
    val cables: MutableSet<BlockPos> = hashSetOf(),
    val machines: MutableMap<BlockPos, EnumSet<Direction>> = hashMapOf()
) : Network(Type.FLUID, world, cables, machines) {

    var tier = Tier.MK1
    private val maxCableTransfer: FluidAmount
        get() = FluidAmount.ofWhole(when (tier) {
            Tier.MK1 -> 1
            Tier.MK2 -> 2
            Tier.MK3 -> 4
            else -> 8
        })

    override fun tick(world: ServerWorld) {
        if (world.time % 20 != 0L) return
        val state = Type.FLUID.getNetworkState(world) as FluidNetworkState
        if (machines.isEmpty()) return
        else if (queue.isEmpty())
            buildQueue()
        if (queue.isNotEmpty()) {
            machines.forEach { (pos, directions) ->
                val originalQueue = queue[pos] ?: return@forEach

                directions.forEach inner@{ dir ->
                    val data = state.endpointData[pos.offset(dir).asLong()]?.get(dir.opposite) ?: return@inner

                    val queue =
                        if (data.mode == FluidEndpointData.Mode.NEAREST_FIRST)
                            PriorityQueue(originalQueue)
                        else
                            PriorityQueue(data.mode.comparator(world)).also { q -> q.addAll(originalQueue) }

                    val output = data.type == FluidEndpointData.Type.OUTPUT
                    if (output)
                        tickOutput(pos, dir, queue, state)
                    else if (data.type == FluidEndpointData.Type.RETRIEVER)
                        tickRetriever(pos, dir, queue, state)

                }
            }
        }
    }

    private fun tickOutput(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: FluidNetworkState) {
        val extractable = extractableOf(world, pos, dir)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining.asInexactDouble() > 1e-9) {
            val (_, targetPos, _, targetDir) = queue.poll()
            val targetData = state.endpointData[targetPos.offset(targetDir).asLong()]?.get(targetDir.opposite)
            val isRetriever = targetData?.type == FluidEndpointData.Type.RETRIEVER
            if (isRetriever) continue

            val insertable = insertableOf(world, targetPos, targetDir)
            val moved = FluidVolumeUtil.move(extractable, insertable, remaining, Simulation.ACTION).amount()
            remaining -= moved
        }
    }

    private fun tickRetriever(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: FluidNetworkState) {
        val insertable = insertableOf(world, pos, dir)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining.asInexactDouble() > 1e-9) {
            val (_, targetPos, _, targetDir) = queue.poll()
            val targetData = state.endpointData[targetPos.offset(targetDir).asLong()]?.get(targetDir.opposite)
            val input = targetData == null || targetData.type == FluidEndpointData.Type.INPUT
            if (!input) continue

            val extractable = extractableOf(world, targetPos, targetDir)
            val moved = FluidVolumeUtil.move(extractable, insertable, remaining, Simulation.ACTION).amount()
            remaining -= moved
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
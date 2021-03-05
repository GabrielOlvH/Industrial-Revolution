package me.steven.indrev.networks.item

import alexiil.mc.lib.attributes.item.ItemInvUtil
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.ItemPipeBlock
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.NetworkState
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.itemExtractableOf
import me.steven.indrev.utils.itemInsertableOf
import net.minecraft.block.Block
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*

class ItemNetwork(
    world: ServerWorld,
    pipes: MutableSet<BlockPos> = hashSetOf(),
    containers: MutableMap<BlockPos, EnumSet<Direction>> = hashMapOf()
) : Network(Type.ITEM, world, pipes, containers) {

    var tier = Tier.MK1
    private val maxCableTransfer: Int
        get() = when (tier) {
            Tier.MK1 -> 32
            Tier.MK2 -> 64
            Tier.MK3 -> 128
            else -> 256
        }

    override fun tick(world: ServerWorld) {
        if (world.time % 20 != 0L) return
        val state = Type.ITEM.getNetworkState(world) as ItemNetworkState
        if (containers.isEmpty()) return
        else if (queue.isEmpty())
            buildQueue()
        if (queue.isNotEmpty()) {
            containers.forEach { (pos, directions) ->
                val originalQueue = queue[pos] ?: return@forEach

                directions.forEach inner@{ dir ->
                    val data = state.endpointData[pos.offset(dir).asLong()]?.get(dir.opposite) as? ItemEndpointData? ?: return@inner
                    if (data.type == EndpointData.Type.INPUT) return@inner
                    val queue =
                        if (data.mode == EndpointData.Mode.NEAREST_FIRST)
                            PriorityQueue(originalQueue)
                        else
                            PriorityQueue(data.mode!!.getItemComparator(world, data.type) { data.matches(it) }).also { q -> q.addAll(originalQueue) }

                    if (data.type == EndpointData.Type.OUTPUT)
                        tickOutput(pos, dir, queue, state, data)
                    else if (data.type == EndpointData.Type.RETRIEVER)
                        tickRetriever(pos, dir, queue, state, data)
                }
            }
        }
    }

    private fun tickOutput(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: ItemNetworkState, data: ItemEndpointData) {
        val extractable = itemExtractableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining > 0) {
            val (_, targetPos, _, targetDir) = queue.poll()
            val targetData = state.endpointData[targetPos.offset(targetDir).asLong()]?.get(targetDir.opposite) as? ItemEndpointData
            val input = targetData == null || targetData.type == EndpointData.Type.INPUT
            if (!input) continue

            val insertable = itemInsertableOf(world, targetPos, targetDir.opposite)
            val moved = ItemInvUtil.move(extractable, insertable, { data.matches(it) && targetData?.matches(it) != false }, remaining)
            remaining -= moved
        }
    }

    private fun tickRetriever(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: ItemNetworkState, data: ItemEndpointData) {
        val insertable = itemInsertableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining > 0) {
            val (_, targetPos, _, targetDir) = queue.poll()
            val targetData = state.endpointData[targetPos.offset(targetDir).asLong()]?.get(targetDir.opposite) as? ItemEndpointData
            val isRetriever = targetData?.type == EndpointData.Type.RETRIEVER
            if (isRetriever) continue

            val extractable = itemExtractableOf(world, targetPos, targetDir.opposite)
            val moved = ItemInvUtil.move(extractable, insertable, { data.matches(it) && targetData?.matches(it) != false }, remaining)
            remaining -= moved
        }
    }

    override fun <T : Network> appendPipe(state: NetworkState<T>, block: Block, blockPos: BlockPos) {
        val cable = block as? ItemPipeBlock ?: return
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

        fun fromTag(world: ServerWorld, tag: CompoundTag): ItemNetwork {
            val network = Network.fromTag(world, tag) as ItemNetwork
            val tier = Tier.values()[tag.getInt("tier")]
            network.tier = tier
            return network
        }
    }
}
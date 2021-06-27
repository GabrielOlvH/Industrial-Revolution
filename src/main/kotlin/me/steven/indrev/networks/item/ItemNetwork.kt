package me.steven.indrev.networks.item

import alexiil.mc.lib.attributes.item.ItemInvUtil
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.ItemPipeBlock
import me.steven.indrev.config.IRConfig
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.Node
import me.steven.indrev.utils.isLoaded
import me.steven.indrev.utils.itemExtractableOf
import me.steven.indrev.utils.itemInsertableOf
import net.minecraft.block.Block
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.*

class ItemNetwork(
    world: ServerWorld,
    pipes: MutableSet<BlockPos> = ObjectOpenHashSet(),
    containers: MutableMap<BlockPos, EnumSet<Direction>> = Object2ObjectOpenHashMap()
) : Network(Type.ITEM, world, pipes, containers) {

    var tier = Tier.MK1
    private val maxCableTransfer: Int
        get() = when (tier) {
            Tier.MK1 -> IRConfig.cables.itemPipeMk1
            Tier.MK2 -> IRConfig.cables.itemPipeMk2
            Tier.MK3 -> IRConfig.cables.itemPipeMk3
            else -> IRConfig.cables.itemPipeMk4
        }

    override fun tick(world: ServerWorld) {
        if (world.time % 20 != 0L) return
        val state = Type.ITEM.getNetworkState(world) as ItemNetworkState
        if (containers.isEmpty()) return
        else if (queue.isEmpty())
            buildQueue()
        if (queue.isNotEmpty()) {
            containers.forEach { (pos, directions) ->
                if (!world.isLoaded(pos)) return@forEach
                val originalQueue = queue[pos] ?: return@forEach

                val sortedQueues = hashMapOf<EndpointData.Mode, PriorityQueue<Node>>()

                directions.forEach inner@{ dir ->
                    val data = state.getEndpointData(pos.offset(dir), dir.opposite) ?: return@inner
                    val filterData = state.getFilterData(pos.offset(dir), dir.opposite)
                    if (data.type == EndpointData.Type.INPUT) return@inner
                    val queue =
                        PriorityQueue(sortedQueues.computeIfAbsent(data.mode!!) {
                            if (data.mode == EndpointData.Mode.NEAREST_FIRST)
                                PriorityQueue(originalQueue)
                            else
                                PriorityQueue(data.mode!!.getItemComparator(world, data.type) { filterData.matches(it) }).also { q -> q.addAll(originalQueue) }
                        })


                    if (data.type == EndpointData.Type.OUTPUT)
                        tickOutput(pos, dir, queue, state, data, filterData)
                    else if (data.type == EndpointData.Type.RETRIEVER)
                        tickRetriever(pos, dir, queue, state, data, filterData)
                }
            }
        }
    }

    private fun tickOutput(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: ItemNetworkState, data: EndpointData, filterData: ItemFilterData) {
        val extractable = itemExtractableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining > 0) {
            val node = queue.poll()
            val (_, targetPos, _, targetDir) = node
            if (!world.isLoaded(targetPos)) continue
            val targetData = state.getEndpointData(targetPos.offset(targetDir), targetDir.opposite)
            val input = targetData == null || targetData.type == EndpointData.Type.INPUT
            if (!input) continue
            val targetFilterData = state.getFilterData(targetPos.offset(targetDir), targetDir.opposite)

            val insertable = itemInsertableOf(world, targetPos, targetDir.opposite)
            val moved = ItemInvUtil.move(extractable, insertable, { filterData.matches(it) && targetFilterData.matches(it) }, remaining)
            remaining -= moved
            if (moved > 0) {
                queue.offer(node)
            }
        }
    }

    private fun tickRetriever(pos: BlockPos, dir: Direction, queue: PriorityQueue<Node>, state: ItemNetworkState, data: EndpointData, filterData: ItemFilterData) {
        val insertable = itemInsertableOf(world, pos, dir.opposite)
        var remaining = maxCableTransfer
        while (queue.isNotEmpty() && remaining > 0) {
            val node = queue.poll()
            val (_, targetPos, _, targetDir) = node
            if (!world.isLoaded(targetPos)) continue
            val targetData = state.getEndpointData(targetPos.offset(targetDir), targetDir.opposite)
            val isRetriever = targetData?.type == EndpointData.Type.RETRIEVER
            if (isRetriever) continue
            val targetFilterData = state.getFilterData(targetPos.offset(targetDir), targetDir.opposite)

            val extractable = itemExtractableOf(world, targetPos, targetDir.opposite)
            val moved = ItemInvUtil.move(extractable, insertable, { filterData.matches(it) && targetFilterData.matches(it) }, remaining)
            remaining -= moved
            if (moved > 0)
                queue.offer(node)
        }
    }

    override fun appendPipe(block: Block, blockPos: BlockPos) {
        val cable = block as? ItemPipeBlock ?: return
        this.tier = cable.tier
        super.appendPipe(block, blockPos)
    }
}
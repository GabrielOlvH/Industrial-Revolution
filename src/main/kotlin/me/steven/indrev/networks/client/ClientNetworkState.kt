package me.steven.indrev.networks.client

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.client.node.ClientNodeInfo
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos

class ClientNetworkState<T : Network>(val type: Network.Type<T>) {

    private val nodes = Long2ObjectOpenHashMap<ClientNodeInfo>()

    fun processPacket(buf: PacketByteBuf, client: MinecraftClient) {

        val positions = hashSetOf<BlockPos>()

        val info = ClientServoNetworkInfo()
        info.read(buf)

        info.pipes.forEach { (pos, info) ->
            val oldInfo = nodes[pos]
            if (info != oldInfo) {
                positions.add(BlockPos.fromLong(pos))
            }
        }
        client.execute {

            val before = nodes.clone()

            nodes.clear()
            nodes.putAll(info.pipes)

            positions.forEach { (x, y, z) ->
                MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(x, y, z, x, y, z)
            }

            before.filterKeys { !positions.contains(BlockPos.fromLong(it)) }.forEach {
                val (x, y, z) = BlockPos.fromLong(it.key)
                MinecraftClient.getInstance().worldRenderer.scheduleBlockRenders(x, y, z, x, y, z)
            }

        }
    }

    fun get(pos: BlockPos): ClientNodeInfo? = nodes[pos.asLong()]

    fun clear() {
        nodes.clear()
    }

    fun add(info: ClientNetworkInfo<*>) {
        info.createNodes().forEach { nodes[it.pos] = it }
    }
}
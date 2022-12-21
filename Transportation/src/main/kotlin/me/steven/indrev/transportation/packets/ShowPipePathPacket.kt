package me.steven.indrev.transportation.packets

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import me.steven.indrev.transportation.networks.Path
import me.steven.indrev.transportation.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object ShowPipePathPacket {
    private val IDENTIFIER = identifier("show_pipe_path")

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(IDENTIFIER) { client, handler, buf, responseSender ->
            val pathsCount = buf.readInt()
            val paths = Object2IntOpenHashMap<Path>()
            repeat(pathsCount) {
                val nodesCount = buf.readInt()
                val nodes = mutableListOf<Long>()
                repeat(nodesCount) {
                    val longPos = buf.readLong()
                    nodes.add(longPos)
                }
                paths[Path(nodes, -1)] = ClientPipeNetworkData.PATH_RENDER_TIME
            }
            client.execute {
                ClientPipeNetworkData.pathsToRender.putAll(paths)
            }
        }
    }
    fun send(player: ServerPlayerEntity, buf: PacketByteBuf) {
        ServerPlayNetworking.send(player, IDENTIFIER, buf)
    }
}
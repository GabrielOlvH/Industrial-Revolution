package me.steven.indrev.transportation.packets

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import me.steven.indrev.transportation.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object AddPipeRenderDataPacket {
    private val IDENTIFIER = identifier("add_pipe_render_packet")

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(IDENTIFIER) { client, handler, buf, responseSender ->
            val count = buf.readInt()
            val newData = Long2IntOpenHashMap()
            repeat(count) { newData[buf.readLong()] = buf.readInt() }
            client.execute {
                  ClientPipeNetworkData.addAll(newData)
            }
        }
    }

    fun send(player: ServerPlayerEntity, buf: PacketByteBuf) {
        ServerPlayNetworking.send(player, IDENTIFIER, buf)
    }
}
package me.steven.indrev.transportation.packets

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import me.steven.indrev.transportation.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object RemovePipeRenderDataPacket {
    private val IDENTIFIER = identifier("rm_pipe_render_packet")

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(IDENTIFIER) { client, handler, buf, responseSender ->
            val count = buf.readInt()
            val newData = LongOpenHashSet()
            repeat(count) { newData.add(buf.readLong()) }
            client.execute {
                ClientPipeNetworkData.removeAll(newData)
            }
        }
    }

    fun send(player: ServerPlayerEntity, buf: PacketByteBuf) {
        ServerPlayNetworking.send(player, IDENTIFIER, buf)
    }
}
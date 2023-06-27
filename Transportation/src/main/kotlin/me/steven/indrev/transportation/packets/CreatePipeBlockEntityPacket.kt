package me.steven.indrev.transportation.packets

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import me.steven.indrev.transportation.blocks.PipeBlock
import me.steven.indrev.transportation.blocks.PipeBlockEntity
import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import me.steven.indrev.transportation.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object CreatePipeBlockEntityPacket {
    private val IDENTIFIER = identifier("add_be_packet")

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(IDENTIFIER) { client, handler, buf, responseSender ->
            val pos = buf.readBlockPos()
            client.execute {
                val state = client.world?.getBlockState(pos) ?: return@execute
                if (state.block is PipeBlock) {
                    val blockEntity = PipeBlockEntity(pos, state)
                    client.world?.addBlockEntity(blockEntity)
                }
            }
        }
    }

    fun send(player: ServerPlayerEntity, buf: PacketByteBuf) {
        ServerPlayNetworking.send(player, IDENTIFIER, buf)
    }
}
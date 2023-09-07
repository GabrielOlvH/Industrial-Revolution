package me.steven.indrev.packets.client

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object SyncEnergyPacket {
    private val PACKET_ID = identifier("sync_energy")

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(PACKET_ID) { client, _, buf, _ ->
            val energy = buf.readLong()
            val pos = buf.readBlockPos()

            client.execute {
                val blockEntity = client.world?.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                blockEntity.energy = energy
            }
        }
    }

    fun send(player: ServerPlayerEntity, buf: PacketByteBuf) {
        ServerPlayNetworking.send(player, PACKET_ID, buf)
    }
}
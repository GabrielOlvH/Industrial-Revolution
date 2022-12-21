package me.steven.indrev.packets.client

import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity

object SyncMachinePropertyPacket {
    private val PACKET_ID = identifier("sync_machine_property")

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(PACKET_ID) { client, _, buf, _ ->
            val size = buf.readInt()
            val sh = client.player?.currentScreenHandler as? MachineScreenHandler ?: return@registerGlobalReceiver

            repeat(size) {
                val index = buf.readInt()
                val syncableObject = sh.properties[index]
                syncableObject.fromPacket(buf)
            }
        }
    }

    fun send(player: ServerPlayerEntity, buf: PacketByteBuf) {
        ServerPlayNetworking.send(player, PACKET_ID, buf)
    }
}
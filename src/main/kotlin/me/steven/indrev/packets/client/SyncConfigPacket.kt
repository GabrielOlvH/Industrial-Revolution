package me.steven.indrev.packets.client

import me.steven.indrev.config.IRConfig
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.network.ServerPlayerEntity

object SyncConfigPacket {

    val SYNC_CONFIG_PACKET = identifier("sync_config_packet") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_CONFIG_PACKET) { _, _, buf, _ ->
            IRConfig.readFromServer(buf)
        }
    }

    fun sendConfig(playerEntity: ServerPlayerEntity) {
        val buf = PacketByteBufs.create()
        IRConfig.writeToClient(buf)
        ServerPlayNetworking.send(playerEntity, SYNC_CONFIG_PACKET, buf)
    }
}
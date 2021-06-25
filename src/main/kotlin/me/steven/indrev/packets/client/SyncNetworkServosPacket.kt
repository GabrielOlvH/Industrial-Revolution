package me.steven.indrev.packets.client

import me.steven.indrev.IndustrialRevolutionClient
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.client.ClientNetworkState
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object SyncNetworkServosPacket {

    val SYNC_NETWORK_SERVOS = identifier("sync_network_servos") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_NETWORK_SERVOS) { client, _, buf, _ ->
            val type = Network.Type.valueOf(buf.readString())
            val state = IndustrialRevolutionClient.CLIENT_NETWORK_STATE.computeIfAbsent(type) { ClientNetworkState(type) }
            state.processPacket(buf, client)
        }
    }
}
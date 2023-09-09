package me.steven.indrev.transportation.events

import me.steven.indrev.transportation.networks.networkManager
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler

object PlayerEvents : ServerPlayConnectionEvents.Join {
    override fun onPlayReady(handler: ServerPlayNetworkHandler, sender: PacketSender, server: MinecraftServer) {
        val player = handler.getPlayer() ?: return
        val world = player.serverWorld ?: return
        world.networkManager.syncAllNetworks(player)
    }
}
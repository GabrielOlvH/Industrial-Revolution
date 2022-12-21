package me.steven.indrev.transportation.events

import me.steven.indrev.transportation.networks.MANAGERS
import me.steven.indrev.transportation.networks.NetworkManager
import me.steven.indrev.transportation.networks.networkManager
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object ServerEvents : ServerLifecycleEvents.ServerStopped, ServerTickEvents.StartWorldTick {
    override fun onStartTick(world: ServerWorld) {
        world.networkManager.networks.forEach {
            if (it.ready) {
                it.tick()
            }
        }
    }

    override fun onServerStopped(server: MinecraftServer?) {
        MANAGERS.clear()
    }
}
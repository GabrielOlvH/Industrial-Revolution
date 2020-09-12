package me.steven.indrev.energy

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object NetworkEvents : ServerTickEvents.EndWorldTick, ServerLifecycleEvents.ServerStopped {
    override fun onEndTick(world: ServerWorld) {
        val networkState = EnergyNetworkState.NETWORK_STATES.computeIfAbsent(world) { EnergyNetworkState.getNetworkState(world) }
        world.profiler.push("indrev_networkTick")
        networkState.networks.forEach { network -> network.tick(world) }
        world.profiler.pop()
    }

    override fun onServerStopped(server: MinecraftServer?) {
        EnergyNetworkState.NETWORK_STATES.clear()
    }
}
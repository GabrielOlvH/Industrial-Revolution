package me.steven.indrev.energy

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object NetworkEvents : ServerTickEvents.EndWorldTick, ServerLifecycleEvents.ServerStopped, ServerLifecycleEvents.ServerStarted {
    override fun onEndTick(world: ServerWorld) {
        EnergyNetworkState.NETWORK_STATES.forEach { (world, networkState) ->
            world.profiler.push("indrev_networkTick")
            networkState.networks.forEach { network -> network.tick(world) }
            world.profiler.pop()
        }
    }

    override fun onServerStarted(server: MinecraftServer?) {
        server?.worlds?.forEach { world ->
            EnergyNetworkState.NETWORK_STATES[world] = EnergyNetworkState.getNetworkState(world)
        }
    }

    override fun onServerStopped(server: MinecraftServer?) {
        EnergyNetworkState.NETWORK_STATES.clear()
    }
}
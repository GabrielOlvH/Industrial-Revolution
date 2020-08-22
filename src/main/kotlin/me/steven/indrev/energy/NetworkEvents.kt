package me.steven.indrev.energy

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object NetworkEvents : ServerTickEvents.EndWorldTick, ServerLifecycleEvents.ServerStopped, ServerLifecycleEvents.ServerStarted {
    override fun onEndTick(world: ServerWorld) {
        EnergyNetworkState.getNetworkState(world).networks.forEach { network -> network.tick(world) }
    }

    override fun onServerStarted(server: MinecraftServer?) {
        server?.worlds?.forEach { world ->
            EnergyNetworkState.getNetworkState(world)
        }
    }

    override fun onServerStopped(server: MinecraftServer?) {
        server?.worlds?.forEach { world ->
            val state = EnergyNetworkState.getNetworkState(world)
            state.networks.clear()
            state.networksByPos.clear()
        }
    }
}
package me.steven.indrev.networks

import me.steven.indrev.networks.fluid.FluidNetwork
import me.steven.indrev.networks.item.ItemNetwork
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld

object NetworkEvents : ServerTickEvents.EndWorldTick, ServerLifecycleEvents.ServerStopped {
    override fun onEndTick(world: ServerWorld) {
        val energyNetworkState = Network.Type.ENERGY.getNetworkState(world)
        world.profiler.push("indrev_energyNetworkTick")
        energyNetworkState.networks.forEach { network -> network.tick(world) }
        world.profiler.pop()

        val fluidNetworkState = Network.Type.FLUID.getNetworkState(world)
        world.profiler.push("indrev_fluidNetworkTick")
        fluidNetworkState.networks.forEach { network -> network.tick(world) }
        (fluidNetworkState as ServoNetworkState<FluidNetwork>).sync(world)
        world.profiler.pop()

        val itemNetworkState = Network.Type.ITEM.getNetworkState(world)
        world.profiler.push("indrev_itemNetworkTick")
        itemNetworkState.networks.forEach { network -> network.tick(world) }
        (itemNetworkState as ServoNetworkState<ItemNetwork>).sync(world)
        world.profiler.pop()
    }

    override fun onServerStopped(server: MinecraftServer?) {
        Network.Type.ENERGY.states.clear()
        Network.Type.FLUID.states.clear()
        Network.Type.ITEM.states.clear()
    }
}
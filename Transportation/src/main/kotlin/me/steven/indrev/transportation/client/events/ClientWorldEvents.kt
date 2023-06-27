package me.steven.indrev.transportation.client.events

import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.world.ClientWorld

object ClientWorldEvents : ClientTickEvents.EndWorldTick {
    override fun onEndTick(world: ClientWorld?) {
        val it = ClientPipeNetworkData.pathsToRender.iterator()
        while (it.hasNext()) {
            val (path, time) = it.next()
            ClientPipeNetworkData.pathsToRender.addTo(path, -1)
            if (time <= 0) {
                it.remove()
            }
        }
    }
}
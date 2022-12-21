package me.steven.indrev.transportation.client.events

import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import java.util.function.LongPredicate

object ClientWorldEvents : ClientTickEvents.EndTick {
    override fun onEndTick(client: MinecraftClient) {
        ClientPipeNetworkData.scheduleUpdates.removeIf(LongPredicate { pos: Long ->
            ClientPipeNetworkData.renderSnapshot[pos] = ClientPipeNetworkData.renderData[pos]
            MinecraftClient.getInstance().worldRenderer.updateBlock(client.world, BlockPos.fromLong(pos), null, null, 8)
            true
        })

        ClientPipeNetworkData.renderSnapshot.long2IntEntrySet().removeIf { (pos, _) -> !ClientPipeNetworkData.renderData.containsKey(pos) }

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
package me.steven.indrev.transportation.client.events

import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import java.util.function.LongPredicate

object ClientEvents : ClientTickEvents.EndTick {
    override fun onEndTick(client: MinecraftClient) {
        ClientPipeNetworkData.scheduleUpdates.removeIf(LongPredicate { pos: Long ->
            ClientPipeNetworkData.renderSnapshot[pos] = ClientPipeNetworkData.renderData[pos]
            MinecraftClient.getInstance().worldRenderer.updateBlock(client.world, BlockPos.fromLong(pos), null, null, 8)
            true
        })

        ClientPipeNetworkData.renderSnapshot.long2IntEntrySet().removeIf { (pos, _) -> !ClientPipeNetworkData.renderData.containsKey(pos) }
    }
}
package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.isLoaded
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object UpdateKnobValue {

    val UPDATE_EFFICIENCY_PACKET = identifier("update_steam_turbine_efficiency")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_EFFICIENCY_PACKET) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val efficiency = buf.readFloat()
            server.execute {
                val world = player.world
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? SteamTurbineBlockEntity ?: return@execute
                    blockEntity.efficiency = efficiency.toDouble()
                    blockEntity.markDirty()
                }
            }
        }
    }
}
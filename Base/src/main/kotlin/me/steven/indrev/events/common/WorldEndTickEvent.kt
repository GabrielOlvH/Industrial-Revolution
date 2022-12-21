package me.steven.indrev.events.common

import me.steven.indrev.packets.client.SyncMachinePropertyPacket
import me.steven.indrev.screens.machine.MachineScreenHandler
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.server.world.ServerWorld

object WorldEndTickEvent : ServerTickEvents.EndWorldTick {
    override fun onEndTick(world: ServerWorld) {
        world.players.forEach { player ->
            val screenHandler = player.currentScreenHandler as? MachineScreenHandler ?: return@forEach
            val toSync = screenHandler.properties.filter { property -> property.isDirty }
            if (toSync.isNotEmpty()) {
                val buf = PacketByteBufs.create()
                buf.writeInt(toSync.size)
                toSync.forEach { property ->
                    buf.writeInt(property.syncId)
                    property.toPacket(buf)
                }
                SyncMachinePropertyPacket.send(player, buf)
            }
        }
    }
}
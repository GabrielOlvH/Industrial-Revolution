package me.steven.indrev.packets.client

import me.steven.indrev.gui.screenhandlers.pipes.PipeFilterScreen
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ClientItemPipePackets {

    val UPDATE_FILTER_SLOT_S2C_PACKET = identifier("update_filter_s2c") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(UPDATE_FILTER_SLOT_S2C_PACKET) { client, _, buf, _ ->
            val slotIndex = buf.readInt()
            val stack = buf.readItemStack()
            client.execute {
                val screen = client.currentScreen as? PipeFilterScreen ?: return@execute
                val controller = screen.screenHandler
                controller.backingList[slotIndex] = stack
            }
        }
    }
}
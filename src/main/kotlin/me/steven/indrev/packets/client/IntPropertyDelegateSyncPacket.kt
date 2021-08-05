package me.steven.indrev.packets.client

import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object IntPropertyDelegateSyncPacket {

    val SYNC_PROPERTY = identifier("sync_property") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_PROPERTY) { client, _, buf, _ ->
            val syncId = buf.readInt()
            val property = buf.readInt()
            val value = buf.readInt()
            client.execute {
                val handler = client.player!!.currentScreenHandler
                if (handler.syncId == syncId)
                    (handler as? IRGuiScreenHandler)?.propertyDelegate?.set(property, value)
            }
        }
    }
}
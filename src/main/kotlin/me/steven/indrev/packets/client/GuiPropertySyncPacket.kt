package me.steven.indrev.packets.client

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.components.SyncableObject
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object GuiPropertySyncPacket {

    val SYNC_PROPERTY = identifier("sync_property")
    val C2S_REQUEST_PROPERTIES = identifier("client_request")

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_PROPERTY) { client, _, buf, _ ->
            val syncId = buf.readInt()
            val property = buf.readInt()

            val handler = client.player!!.currentScreenHandler
            if (handler.syncId == syncId && handler is IRGuiScreenHandler) {
                val prop = handler.component?.properties?.get(property) ?: return@registerGlobalReceiver
                prop.fromPacket(buf)
                handler.onSyncedProperty(property, prop)
            } else {
                IndustrialRevolution.LOGGER.warn("Received sync packet for unknown screen type @ $handler")
            }
        }
    }

    fun registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(C2S_REQUEST_PROPERTIES) { server, player, _, buf, _ ->
            val syncId = buf.readInt()

            val handler = player!!.currentScreenHandler
            if (handler.syncId == syncId && handler is IRGuiScreenHandler) {
                handler.component!!.properties.forEach { it.markDirty() }
            }
        }
    }
}
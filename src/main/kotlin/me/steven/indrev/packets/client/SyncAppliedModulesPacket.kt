package me.steven.indrev.packets.client

import me.steven.indrev.api.IRPlayerEntityExtension
import me.steven.indrev.tools.modular.ArmorModule
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object SyncAppliedModulesPacket {

    val SYNC_MODULE_PACKET = identifier("sync_module") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_MODULE_PACKET) { client, _, buf, _ ->
            val size = buf.readInt()
            val modules = hashMapOf<ArmorModule, Int>()
            for (index in 0 until size) {
                val ordinal = buf.readInt()
                val module = ArmorModule.values()[ordinal]
                val level = buf.readInt()
                modules[module] = level
            }
            val durability = buf.readDouble()
            val isRegenerating = buf.readBoolean()
            client.execute {
                val player = client.player!!
                if (player is IRPlayerEntityExtension) {
                    (player.getAppliedModules() as MutableMap<*, *>).clear()
                    modules.forEach(player::applyModule)
                    player.shieldDurability = durability
                    player.isRegenerating = isRegenerating
                }
            }
        }
    }
}
package me.steven.indrev.packets.common

import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object UpdateModularToolLevelPacket  {

    val UPDATE_MODULAR_TOOL_LEVEL = identifier("update_modular_level") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_MODULAR_TOOL_LEVEL) { server, player, _, buf, _ ->
            val key = buf.readString(32767)
            val value = buf.readInt()
            val slot = buf.readInt()
            server.execute {
                val stack = player.inventory.getStack(slot)
                if (!stack.isEmpty) {
                    val tag = stack.getOrCreateSubNbt("selected")
                    tag.putInt(key, value)
                }
            }
        }
    }
}
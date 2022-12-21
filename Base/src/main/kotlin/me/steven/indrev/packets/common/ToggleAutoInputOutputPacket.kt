package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.ConfigurationTypes
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf

object ToggleAutoInputOutputPacket {
    private val PACKET_ID = identifier("toggle_auto_io")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val autoInput = buf.readBoolean()
            val autoOutput = buf.readBoolean()
            val type = buf.readEnumConstant(ConfigurationTypes::class.java)

            server.execute {
                val blockEntity = player.world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                type.provider(blockEntity).autoInput = autoInput
                type.provider(blockEntity).autoOutput = autoOutput
            }
        }
    }

    fun send(buf: PacketByteBuf) {
        ClientPlayNetworking.send(PACKET_ID, buf)
    }
}
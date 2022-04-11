package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.miningrig.DataCardWriterBlockEntity
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object DataCardWriteStartPacket {

    val START_PACKET = identifier("write_data_card_start")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(START_PACKET) { server, player, handler, buf, responseSender ->
            val pos = buf.readBlockPos()
            server.execute {
                val blockEntity = player.world.getBlockEntity(pos) as? DataCardWriterBlockEntity ?: return@execute
                blockEntity.start()
            }
        }
    }
}
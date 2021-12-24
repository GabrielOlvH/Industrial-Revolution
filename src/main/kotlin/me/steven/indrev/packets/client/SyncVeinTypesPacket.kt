package me.steven.indrev.packets.client

import io.netty.buffer.Unpooled
import me.steven.indrev.utils.entries
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.chunkveins.VeinType
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.block.Block
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.collection.WeightedList
import net.minecraft.util.registry.Registry

object SyncVeinTypesPacket {

    val SYNC_VEINS_PACKET = identifier("sync_veins_packet") 

     fun register() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_VEINS_PACKET) { _, _, buf, _ ->
            val totalVeins = buf.readInt()
            for (x in 0 until totalVeins) {
                val id = buf.readIdentifier()
                val entriesSize = buf.readInt()
                val outputs = WeightedList<Block>()
                val infiniteOutputs = WeightedList<Block>()
                for (y in 0 until entriesSize) {
                    val id = buf.readIdentifier()
                    val weight = buf.readInt()
                    val infiniteWeight = buf.readInt()
                    val block = Registry.BLOCK.get(id)
                    outputs.add(block, weight)
                    infiniteOutputs.add(block, infiniteWeight)
                }
                val minSize = buf.readInt()
                val maxSize = buf.readInt()
                val veinType = VeinType(id, outputs, infiniteOutputs, minSize..maxSize)
                VeinType.REGISTERED[id] = veinType
            }
        }
    }

    fun sendVeinTypes(playerEntity: ServerPlayerEntity) {
        val buf = PacketByteBuf(Unpooled.buffer())
        buf.writeInt(VeinType.REGISTERED.size)
        VeinType.REGISTERED.forEach { (identifier, veinType) ->
            buf.writeIdentifier(identifier)
            val entries = veinType.outputs.entries
            val infiniteEntries = veinType.infiniteOutputs.entries
            buf.writeInt(entries.size)

            for (i in 0 until entries.size) {
                val entry = entries[i]
                val block = entry.element
                val weight = entry.weight
                val infiniteWeight = infiniteEntries[i].weight
                val id = Registry.BLOCK.getId(block)
                buf.writeIdentifier(id)
                buf.writeInt(weight)
                buf.writeInt(infiniteWeight)
            }
            buf.writeInt(veinType.sizeRange.first)
            buf.writeInt(veinType.sizeRange.last)
        }
        ServerPlayNetworking.send(playerEntity, SYNC_VEINS_PACKET, buf)
    }
}

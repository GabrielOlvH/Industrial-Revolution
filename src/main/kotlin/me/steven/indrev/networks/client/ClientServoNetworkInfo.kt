package me.steven.indrev.networks.client

import com.google.common.collect.ImmutableList
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.client.node.ClientServoNodeInfo
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Direction

class ClientServoNetworkInfo : ClientNetworkInfo<ClientServoNodeInfo> {

    val pipes = Long2ObjectOpenHashMap<ClientServoNodeInfo>()
    override fun write(buf: PacketByteBuf) {
        buf.writeInt(pipes.size)
        pipes.forEach { (pos, info) ->
            buf.writeLong(pos)
            buf.writeByte(info.size)
            info.forEach { dir, data ->
                buf.writeByte(dir.id)
                buf.writeByte(data.ordinal)
            }
        }
    }

    @Environment(EnvType.CLIENT)
    override fun read(buf: PacketByteBuf) {
        pipes.clear()
        val size = buf.readInt()
        repeat(size) {
            val pos = buf.readLong()
            val info = ClientServoNodeInfo(pos, Object2ObjectOpenHashMap())
            val infoSize = buf.readByte()
            repeat(infoSize.toInt()) {
                val dir = Direction.byId(buf.readByte().toInt())
                val type = EndpointData.Type.VALUES[buf.readByte().toInt()]
                info.servos[dir] = type
            }
            pipes[pos] = info

        }
    }

    override fun createNodes(): List<ClientServoNodeInfo> {
        val list = ImmutableList.builder<ClientServoNodeInfo>()
        pipes.forEach { (_, servos) -> list.add(servos) }
        return list.build()
    }
}
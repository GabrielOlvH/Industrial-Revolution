package me.steven.indrev.networks.client

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf

class ClientEnergyNetworkInfo : ClientNetworkInfo {

    override var version = 0

    val cables = LongOpenHashSet()

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(cables.size)
        cables.forEach { pos ->
            buf.writeLong(pos)
        }
    }

    @Environment(EnvType.CLIENT)
    override fun read(buf: PacketByteBuf) {
        cables.clear()
        val size = buf.readInt()
        repeat(size) { cables.add(buf.readLong()) }
    }
}
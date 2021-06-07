package me.steven.indrev.networks.client

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf
interface ClientNetworkInfo {

    var version: Int

    fun write(buf: PacketByteBuf)

    @Environment(EnvType.CLIENT)
    fun read(buf: PacketByteBuf)
}
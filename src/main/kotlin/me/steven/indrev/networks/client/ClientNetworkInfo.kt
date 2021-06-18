package me.steven.indrev.networks.client

import me.steven.indrev.networks.client.node.ClientNodeInfo
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.network.PacketByteBuf

interface ClientNetworkInfo<T : ClientNodeInfo> {

    fun write(buf: PacketByteBuf)

    @Environment(EnvType.CLIENT)
    fun read(buf: PacketByteBuf)

    fun createNodes(): List<T>
}
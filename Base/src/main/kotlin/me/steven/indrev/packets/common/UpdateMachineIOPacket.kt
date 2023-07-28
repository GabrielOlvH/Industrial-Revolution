package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainer
import me.steven.indrev.utils.ConfigurationTypes
import me.steven.indrev.utils.SidedConfiguration
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Direction

object UpdateMachineIOPacket {
    private val PACKET_ID = identifier("update_machine_io")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val type = ConfigurationTypes.values()[buf.readInt()]
            val dir = Direction.byId(buf.readInt())
            val mode = SidedConfiguration.Mode.BY_ID[buf.readInt()]
            server.execute {
                val blockEntity = player.world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute

                if (type == ConfigurationTypes.ITEM && blockEntity.inventory.exists()) {
                    blockEntity.inventory.sidedConfiguration.setMode(dir, mode)
                } else if (type == ConfigurationTypes.FLUID && blockEntity.fluidInventory.exists()) {
                    blockEntity.fluidInventory.sidedConfiguration.setMode(dir, mode)
                } else if (type == ConfigurationTypes.ENERGY && blockEntity is LazuliFluxContainer) {
                    blockEntity.sideConfig.setMode(dir, mode)
                } else return@execute
                blockEntity.markDirty()
                blockEntity.world?.updateNeighbors(pos, blockEntity.cachedState.block)
            }
        }
    }

    fun send(buf: PacketByteBuf) {
        ClientPlayNetworking.send(PACKET_ID, buf)
    }
}
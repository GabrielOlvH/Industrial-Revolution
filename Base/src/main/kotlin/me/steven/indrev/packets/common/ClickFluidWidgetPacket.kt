package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.network.PacketByteBuf

object ClickFluidWidgetPacket {

    private val PACKET_ID = identifier("click_fluid_widget")

    fun register() {
        ServerPlayNetworking.registerGlobalReceiver(PACKET_ID) { server, player, _, buf, _ ->
            val tankSlot = buf.readInt()
            val pos = buf.readBlockPos()
            server.execute {
                val blockEntity = player.world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                if (blockEntity.fluidInventory.exists()) {
                    val handStorage = ContainerItemContext.ofPlayerCursor(player, player.currentScreenHandler).find(FluidStorage.ITEM) ?: return@execute
                    val fluidSlot = blockEntity.fluidInventory[tankSlot]
                    val moved = StorageUtil.move(handStorage, fluidSlot, { true }, Long.MAX_VALUE, null)
                    if (moved == 0L) {
                        StorageUtil.move(fluidSlot, handStorage, { true }, Long.MAX_VALUE, null)
                    }
                }
            }
        }
    }

    fun send(buf: PacketByteBuf) {
        ClientPlayNetworking.send(PACKET_ID, buf)
    }
}
package me.steven.indrev.packets.common

import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.item.ItemNetworkState
import me.steven.indrev.packets.client.ClientItemPipePackets
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Direction

object ItemPipePackets  {

    val CLICK_FILTER_SLOT_PACKET = identifier("click_filter_slot")
    val CHANGE_FILTER_MODE_PACKET = identifier("change_whitelist_mode")
    val CHANGE_SERVO_MODE_PACKET = identifier("change_servo_mode") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(CLICK_FILTER_SLOT_PACKET) { server, player, _, buf, _ ->
            val slotIndex = buf.readInt()
            val dir = buf.readEnumConstant(Direction::class.java)
            val pos = buf.readBlockPos()
            server.execute {
                val cursorStack = player.currentScreenHandler.cursorStack
                val state = Network.Type.ITEM.getNetworkState(player.serverWorld) as? ItemNetworkState ?: return@execute
                val data = state.getFilterData(pos, dir)
                if (cursorStack.isEmpty) data.filter[slotIndex] = ItemStack.EMPTY
                else data.filter[slotIndex] = cursorStack.copy().also { it.count = 1 }
                state.markDirty()
                val syncPacket = PacketByteBufs.create()
                syncPacket.writeInt(slotIndex)
                syncPacket.writeItemStack(data.filter[slotIndex])
                ServerPlayNetworking.send(player, ClientItemPipePackets.UPDATE_FILTER_SLOT_S2C_PACKET, syncPacket)
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(CHANGE_FILTER_MODE_PACKET) { server, player, _, buf, _ ->
            val dir = buf.readEnumConstant(Direction::class.java)
            val pos = buf.readBlockPos()
            val field = buf.readInt()
            val value = buf.readBoolean()

            server.execute {
                val state = Network.Type.ITEM.getNetworkState(player.serverWorld) as? ItemNetworkState ?: return@execute
                val data = state.getFilterData(pos, dir, true)
                when (field) {
                    0 -> data.whitelist = value
                    1 -> data.matchDurability = value
                    2 -> data.matchTag = value
                    else -> return@execute
                }
                state.markDirty()
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(CHANGE_SERVO_MODE_PACKET) { server, player, _, buf, _ ->
            val dir = buf.readEnumConstant(Direction::class.java)
            val pos = buf.readBlockPos()
            val mode = buf.readEnumConstant(EndpointData.Mode::class.java)

            server.execute {
                val state = Network.Type.ITEM.getNetworkState(player.serverWorld) as? ItemNetworkState ?: return@execute
                val data = state.getEndpointData(pos, dir, true) ?: return@execute
                data.mode = mode
                state.markDirty()
            }
        }
    }
}
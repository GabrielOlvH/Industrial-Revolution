package me.steven.indrev.packets.common

import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.GlobalStateController
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.networks.Network
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Direction

object ConfigureIOPackets  {

    val UPDATE_MACHINE_SIDE_PACKET_ID = identifier("update_machine_side")
    val UPDATE_AUTO_OPERATION_PACKET_ID = identifier("update_auto_pull_push") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(UPDATE_MACHINE_SIDE_PACKET_ID) { server, player, _, buf, _ ->
            val type = buf.readEnumConstant(ConfigurationType::class.java)
            val pos = buf.readBlockPos()
            val dir = Direction.byId(buf.readInt())
            val mode = TransferMode.values()[buf.readInt()]
            server.execute {
                val world = player.world
                val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                blockEntity.getCurrentConfiguration(type)[dir] = mode
                blockEntity.markDirty()
                GlobalStateController.update(world, pos, false)
                world.updateNeighbors(pos, blockEntity.cachedState.block)
                val networkState = Network.Type.ENERGY.getNetworkState(world as ServerWorld)
                Network.handleUpdate(networkState, pos.offset(dir))
            }
        }

        ServerPlayNetworking.registerGlobalReceiver(UPDATE_AUTO_OPERATION_PACKET_ID) { server, player, _, buf, _ ->
            val type = buf.readEnumConstant(ConfigurationType::class.java)
            val opType = buf.readByte()
            val pos = buf.readBlockPos()
            val value = buf.readBoolean()
            server.execute {
                val world = player.world
                val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return@execute
                if (opType.toInt() == 0)
                    blockEntity.getCurrentConfiguration(type).autoPush = value
                else
                    blockEntity.getCurrentConfiguration(type).autoPull = value
                blockEntity.markDirty()
            }
        }
    }
}
package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.crafters.CraftingMachineBlockEntity
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.isLoaded
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object ToggleFactoryStackSplittingPacket  {

    val SPLIT_STACKS_PACKET = identifier("split_stacks_packet") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(SPLIT_STACKS_PACKET) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            server.execute {
                val world = player.world
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? CraftingMachineBlockEntity<*> ?: return@execute
                    blockEntity.isSplitOn = !blockEntity.isSplitOn
                    if (blockEntity.isSplitOn) blockEntity.splitStacks()
                }
            }
        }

    }
}
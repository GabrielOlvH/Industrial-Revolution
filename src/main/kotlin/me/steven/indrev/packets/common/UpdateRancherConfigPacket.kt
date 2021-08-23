package me.steven.indrev.packets.common

import me.steven.indrev.blockentities.farms.RancherBlockEntity
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.isLoaded
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object UpdateRancherConfigPacket  {

    val SYNC_RANCHER_CONFIG = identifier("rancher_sync_config") 

     fun register() {
        ServerPlayNetworking.registerGlobalReceiver(SYNC_RANCHER_CONFIG) { server, player, _, buf, _ ->
            val pos = buf.readBlockPos()
            val feedBabies = buf.readBoolean()
            val mateAdults = buf.readBoolean()
            val matingLimit = buf.readInt()
            val killAfter = buf.readInt()
            server.execute {
                val world = player.world
                if (world.isLoaded(pos)) {
                    val blockEntity = world.getBlockEntity(pos) as? RancherBlockEntity ?: return@execute
                    blockEntity.feedBabies = feedBabies
                    blockEntity.mateAdults = mateAdults
                    blockEntity.matingLimit = matingLimit
                    blockEntity.killAfter = killAfter
                    blockEntity.markDirty()
                }
            }
        }
    }
}
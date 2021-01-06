package me.steven.indrev.blockentities

import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import me.steven.indrev.utils.identifier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World

object GlobalStateController {

    val UPDATE_PACKET_ID = identifier("global_state_update")

    @Environment(EnvType.CLIENT)
    val chunksToUpdate = hashSetOf<ChunkPos>()

    fun update(world: World, pos: BlockPos, workingState: Boolean) {
        val (x, y, z) = pos
        val players = (world as ServerWorld).server.playerManager.playerList
        for (i in players.indices) {
            val serverPlayerEntity = players[i]
            if (serverPlayerEntity.world.registryKey === world.registryKey) {
                val xOffset = x - serverPlayerEntity.x
                val yOffset = y - serverPlayerEntity.y
                val zOffset = z - serverPlayerEntity.z
                if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset < 64 * 64) {
                    val buf = PacketByteBufs.create()
                    buf.writeLong(pos.asLong())
                    buf.writeBoolean(workingState)
                    ServerPlayNetworking.send(serverPlayerEntity, UPDATE_PACKET_ID, buf)
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun initClient() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client != null) {
                chunksToUpdate.removeIf { chunkPos ->
                    client.worldRenderer.updateBlock(client.world, chunkPos.startPos, null, null, 8)
                    true
                }
            }
        }
    }

}
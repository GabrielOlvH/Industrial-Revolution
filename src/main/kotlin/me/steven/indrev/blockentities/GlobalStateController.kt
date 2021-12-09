package me.steven.indrev.blockentities

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import me.steven.indrev.packets.client.MachineStateUpdatePacket
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.World
import java.util.function.LongFunction

object GlobalStateController {
    @Environment(EnvType.CLIENT)
    val chunksToUpdate: Long2ObjectMap<MutableSet<BlockPos>> = Long2ObjectOpenHashMap()
    @Environment(EnvType.CLIENT)
    val workingStateTracker = Long2BooleanOpenHashMap()

    @Environment(EnvType.CLIENT)
    fun queueUpdate(pos: BlockPos) {
        val chunkPos = ChunkPos.toLong(pos.x shr 4, pos.z shr 4)
        if (MinecraftClient.getInstance().isOnThread)
            chunksToUpdate.computeIfAbsent(chunkPos, LongFunction { hashSetOf() }).add(pos)
        else
            MinecraftClient.getInstance().execute { chunksToUpdate.computeIfAbsent(chunkPos, LongFunction { hashSetOf() }).add(pos) }
    }

    fun update(world: World, pos: BlockPos, workingState: Boolean) {
        val (x, y, z) = pos
        val players = world.server!!.playerManager.playerList
        for (i in players.indices) {
            val player = players[i]
            if (player.world.registryKey === world.registryKey) {
                val xOffset = x - player.x
                val yOffset = y - player.y
                val zOffset = z - player.z
                if (xOffset * xOffset + yOffset * yOffset + zOffset * zOffset < 64 * 64) {
                    val buf = PacketByteBufs.create()
                    buf.writeLong(pos.asLong())
                    buf.writeBoolean(workingState)
                    ServerPlayNetworking.send(player, MachineStateUpdatePacket.UPDATE_PACKET_ID, buf)
                }
            }
        }
    }

    @Environment(EnvType.CLIENT)
    fun initClient() {
        var ticks = 0
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            ticks++
            val world = client.world
            if (world != null && ticks % 15 == 0) {
                chunksToUpdate.values.removeIf { positions ->
                    positions.forEach { (x, y, z) ->
                        client.worldRenderer.scheduleBlockRenders(x , y, z, x, y,z)
                    }
                    true
                }
            }
        }
    }
}
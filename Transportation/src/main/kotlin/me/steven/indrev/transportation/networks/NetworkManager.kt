package me.steven.indrev.transportation.networks

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.transportation.blocks.PipeBlock
import me.steven.indrev.transportation.networks.types.PipeNetwork
import me.steven.indrev.transportation.packets.RemovePipeRenderDataPacket
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

val MANAGERS = Object2ObjectOpenHashMap<World, NetworkManager>()

val ServerWorld.networkManager: NetworkManager
    get() = MANAGERS.computeIfAbsent(this, Object2ObjectFunction { NetworkManager(this) })

class NetworkManager(private val world: ServerWorld) {

    val networks = mutableListOf<PipeNetwork<*>>()
    val networksByPos = Long2ObjectOpenHashMap<PipeNetwork<*>>()

    val scheduledUpdates = LongOpenHashSet()

    fun runScheduledUpdates() {
        val mutable = BlockPos.Mutable()
        scheduledUpdates.forEach { longPos ->
            if (networksByPos.contains(longPos)) return@forEach
            mutable.set(longPos)
            val block = world.getBlockState(mutable).block
            if (block is PipeBlock)
                createNetwork(this, mutable, world, block)
        }
        scheduledUpdates.clear()
    }

    fun syncAllNetworks(player: ServerPlayerEntity) {
        networks.forEach { network -> network.sync(player) }
    }

    fun syncToAllPlayers(network: PipeNetwork<*>) {
        world.players.forEach { player -> network.sync(player) }
    }

    fun remove(network: PipeNetwork<*>) {
        val removed = networks.remove(network)
        network.nodes.forEach { networksByPos.remove(it.key) }

        if (removed) {
            val buf = PacketByteBufs.create()
            buf.writeInt(network.nodes.size)
            network.nodes.forEach { (pos, _) -> buf.writeLong(pos) }
            world.players.forEach { player ->
                RemovePipeRenderDataPacket.send(player, buf)
            }
        }
    }
}
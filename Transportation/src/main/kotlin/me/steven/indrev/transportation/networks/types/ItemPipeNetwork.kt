package me.steven.indrev.transportation.networks.types

import me.steven.indrev.transportation.networks.Path
import me.steven.indrev.transportation.packets.ShowPipePathPacket
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.function.LongFunction

class ItemPipeNetwork(world: ServerWorld) : StoragePipeNetwork<ItemVariant>(world) {

    private val pathsToRender = mutableListOf<Path>()

    override val minimumTransferable: Long = 1L
    override val maximumTransferable: Long = 64L

    override fun tick() {
        super.tick()
        world.players.forEach { player -> sendPathsToPlayer(player) }

        pathsToRender.clear()
    }

    override fun onMove(resource: ItemVariant, path: Path) {
        pathsToRender.add(path)
    }

    private fun sendPathsToPlayer(playerEntity: ServerPlayerEntity) {
        val buf = PacketByteBufs.create()
        buf.writeInt(pathsToRender.size)
        pathsToRender.forEach { path ->
            buf.writeInt(path.nodes.size)
            path.nodes.forEach { node -> buf.writeLong(node) }
        }
        ShowPipePathPacket.send(playerEntity, buf)
    }

    override fun find(world: ServerWorld, pos: BlockPos, direction: Direction): Storage<ItemVariant>? {
        return apiCache.computeIfAbsent(pos.asLong(), LongFunction {
            BlockApiCache.create(ItemStorage.SIDED, world, pos)
        }).find(direction)
    }
}
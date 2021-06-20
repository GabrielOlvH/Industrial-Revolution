package me.steven.indrev.networks

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

open class NetworkState<T : Network>(val type: Network.Type<T>, val world: ServerWorld) : PersistentState() {

    val networksByPos = Long2ObjectOpenHashMap<T>()
    val networks = ObjectOpenHashSet<Network>()

    val queuedUpdates = LongOpenHashSet()
    val updatedPositions = LongOpenHashSet()


    fun queueUpdate(pos: Long, force: Boolean = false) {
        if (!networksByPos.contains(pos) || force) queuedUpdates.add(pos)
    }

    operator fun set(pos: BlockPos, network: Network) {
        networksByPos[pos.asLong()] = network as T
    }

    fun remove(network: Network) {
        network.pipes.forEach { pos ->
            networksByPos.remove(pos.asLong())
            onRemoved(pos)
        }
        network.containers.forEach { (pos, _) ->
            networksByPos.remove(pos.asLong())
            onRemoved(pos)
        }
        networks.remove(network)
    }

    open fun tick(world: ServerWorld) {
        LongOpenHashSet(queuedUpdates).forEach { pos ->
            if (!updatedPositions.contains(pos)) {
                val network = type.factory.deepScan(type, world, BlockPos.fromLong(pos))
                if (network.pipes.isNotEmpty())
                    networks.add(network)
                else
                    remove(network)
            }
        }
        queuedUpdates.clear()
        updatedPositions.clear()
        world.profiler.push("indrev_${type.key.lowercase()}NetworkTick")
        networks.forEach { network -> network.tick(world) }
        world.profiler.pop()

    }

    fun clear() {
        this.queuedUpdates.clear()
        this.updatedPositions.clear()
        this.networksByPos.clear()
        this.networks.clear()
    }

    open fun onRemoved(pos: BlockPos) {
    }

    open fun onSet(blockPos: BlockPos, network: T) {
    }


    override fun writeNbt(tag: NbtCompound): NbtCompound {

        return tag
    }

    companion object {
        const val ENERGY_KEY = "indrev_networks"
        const val FLUID_KEY = "indrev_fluid_networks"
        const val ITEM_KEY = "indrev_item_networks"
    }
}
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

    private val queuedUpdates = LongOpenHashSet()
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
        // this is to avoid a weird CME I cannot reproduce
        // something is queueing an update while going through the other ones (nothing is ever removed from it until the .clear())
        // so I copy and clear the original before iterating so if anything is added while iterating, it will be processed in the next tick.
        // TODO find this CME and fix it :)
        val copy = LongOpenHashSet(queuedUpdates)
        queuedUpdates.clear()
        copy.forEach { pos ->
            if (!updatedPositions.contains(pos)) {
                val network = type.factory.deepScan(type, world, BlockPos.fromLong(pos))
                if (network.pipes.isNotEmpty())
                    networks.add(network)
                else
                    remove(network)
            }
        }
        updatedPositions.clear()
        world.profiler.push("indrev_${type.key}NetworkTick")
        networks.forEach { network -> network.tick(world) }
        world.profiler.pop()

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
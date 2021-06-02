package me.steven.indrev.networks

import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

open class NetworkState<T : Network>(val type: Network.Type<T>, val world: ServerWorld) : PersistentState() {
    var networks = hashSetOf<T>()
    protected val networksByPos = hashMapOf<BlockPos, T>()

    open fun remove(pos: BlockPos) {
        networksByPos.remove(pos)
    }

    fun contains(pos: BlockPos) = networksByPos.containsKey(pos)

    open operator fun set(blockPos: BlockPos, network: T) {
        networksByPos[blockPos] = network
    }

    operator fun get(pos: BlockPos): T? = networksByPos[pos]

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        val list = NbtList()
        networks.map {
            val networkTag = NbtCompound()
            it.writeNbt(networkTag)
            networkTag
        }.forEach { list.add(it) }
        tag.put("networks", list)
        return tag
    }

    companion object {
        const val ENERGY_KEY = "indrev_networks"
        const val FLUID_KEY = "indrev_fluid_networks"
        const val ITEM_KEY = "indrev_item_networks"

        fun <T : Network> readNbt(tag: NbtCompound, supplier: () -> NetworkState<T>): NetworkState<T> {
            val state = supplier()
            val list = tag.getList("networks", 10)
            state.networks = list.map { networkTag ->
                state.type.createEmpty(state.world).also { it.readNbt(state.world, networkTag as NbtCompound) }
            }.toHashSet()
            state.networks.forEach { network ->
                network.pipes.forEach { pos -> state.networksByPos[pos] = network }
            }
            return state
        }
    }
}
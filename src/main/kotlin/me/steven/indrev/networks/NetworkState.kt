package me.steven.indrev.networks

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

class NetworkState<T : Network>(private val type: Network.Type<T>, private val world: ServerWorld, key: String) : PersistentState(key) {
    var networks = hashSetOf<T>()
    val networksByPos = hashMapOf<BlockPos, T>()

    override fun toTag(tag: CompoundTag): CompoundTag {
        val list = ListTag()
        networks.map {
            val networkTag = CompoundTag()
            it.toTag(networkTag)
            networkTag
        }.forEach { list.add(it) }
        tag.put("networks", list)
        return tag
    }

    override fun fromTag(tag: CompoundTag) {
        val list = tag.getList("networks", 10)
        networks = list.map { networkTag ->
            type.createEmpty(world).also { it.fromTag(world, networkTag as CompoundTag) }
        }.toHashSet()
        networks.forEach { network ->
            network.pipes.forEach { pos -> networksByPos[pos] = network }
        }
    }

    companion object {
        const val ENERGY_KEY = "indrev_networks"
        const val FLUID_KEY = "indrev_fluid_networks"
        const val ITEM_KEY = "indrev_item_networks"
    }
}
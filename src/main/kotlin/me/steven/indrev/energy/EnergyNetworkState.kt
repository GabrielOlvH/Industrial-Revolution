package me.steven.indrev.energy

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState
import java.util.*

class EnergyNetworkState(private val world: ServerWorld) : PersistentState("indrev_networks") {
    var networks = hashSetOf<EnergyNetwork>()
    val networksByPos = hashMapOf<BlockPos, EnergyNetwork>()

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
            EnergyNetwork.fromTag(world, networkTag as CompoundTag)
        }.toHashSet()
        networks.forEach { network ->
            network.cables.forEach { pos -> networksByPos[pos] = network }
        }
    }

    companion object {
        val NETWORK_STATES = WeakHashMap<ServerWorld, EnergyNetworkState>()
        fun getNetworkState(world: ServerWorld): EnergyNetworkState = world.persistentStateManager.getOrCreate({ EnergyNetworkState(world) }, "indrev_networks")
    }
}
package me.steven.indrev.energy

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

class EnergyNetworkState(private val world: ServerWorld) : PersistentState("indrev_networks") {
    var networks = mutableSetOf<EnergyNetwork>()
    val networksByPos = mutableMapOf<BlockPos, EnergyNetwork>()

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
        }.toMutableSet()
        networks.forEach { network ->
            network.cables.forEach { pos -> networksByPos[pos] = network }
        }
    }

    companion object {
        fun getNetworkState(world: ServerWorld) = world.persistentStateManager.getOrCreate({ EnergyNetworkState(world) }, "indrev_networks")
    }
}
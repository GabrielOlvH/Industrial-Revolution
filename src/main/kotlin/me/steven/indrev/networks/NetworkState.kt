package me.steven.indrev.networks

import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.world.PersistentState

open class NetworkState<T : Network>(val type: Network.Type<T>, val world: ServerWorld) : PersistentState() {

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
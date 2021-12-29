package me.steven.indrev.networks.energy

import me.steven.indrev.networks.Network
import me.steven.indrev.networks.NetworkState
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.NbtList
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class EnergyNetworkState(world: ServerWorld) : NetworkState<EnergyNetwork>(Network.Type.ENERGY, world) {

    private var destroyedEnergy = 0L

    val savedEnergy = mutableMapOf<BlockPos, Long>()

    override fun add(network: Network) {
        super.add(network)

        if (network is EnergyNetwork) {
            network.energy += destroyedEnergy.coerceAtMost(network.capacity)
            destroyedEnergy -= network.energy

            if (destroyedEnergy <= 0) destroyedEnergy = 0
        }
    }

    override fun remove(network: Network) {
        super.remove(network)

        if (network is EnergyNetwork) {
            destroyedEnergy += network.energy
        }
    }

    override fun tick(world: ServerWorld) {
        super.tick(world)
        destroyedEnergy = 0
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        val list = NbtList()
        networks.forEach { network ->
            val pos = network.pipes.minByOrNull { it }  ?: return@forEach
            val networkTag = NbtCompound()
            networkTag.put("Pos", NbtHelper.fromBlockPos(pos))
            networkTag.putLong("Energy", (network as EnergyNetwork).energy)
            list.add(networkTag)
        }
        tag.put("SavedEnergy", list)
        return super.writeNbt(tag)
    }


    companion object {
        fun readNbt(tag: NbtCompound, supplier: () -> EnergyNetworkState): EnergyNetworkState {
            val state = supplier()
            val list = tag.getList("SavedEnergy", 10)

            list.forEach { t ->
                val networkTag = t as NbtCompound
                val pos = NbtHelper.toBlockPos(networkTag.getCompound("Pos"))
                val energy = networkTag.getLong("Energy")
                state.savedEnergy[pos] = energy
            }

            return state
        }
    }
}
package me.steven.indrev.networks.energy

import me.steven.indrev.networks.Network
import me.steven.indrev.networks.NetworkState
import net.minecraft.server.world.ServerWorld

class EnergyNetworkState(world: ServerWorld) : NetworkState<EnergyNetwork>(Network.Type.ENERGY, world) {

    private var destroyedEnergy = 0L

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
}
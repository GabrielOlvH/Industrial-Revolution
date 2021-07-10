package me.steven.indrev.networks.energy

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo

class CableEnergyIo(private val network: EnergyNetwork?) : EnergyIo {

    override fun getEnergy(): Double = network?.energy ?: 0.0

    override fun getEnergyCapacity(): Double = network?.capacity ?: 0.0

    override fun insert(amount: Double, simulation: Simulation): Double {
        if (network == null) return amount
        val inserted = amount.coerceAtMost(network.maxCableTransfer).coerceAtMost(this.energyCapacity - energy)
        if (simulation.isActing) network.energy += inserted
        return amount - inserted
    }

    override fun supportsInsertion(): Boolean = true

    override fun supportsExtraction(): Boolean = false

    companion object {
        val NO_NETWORK = CableEnergyIo(null)
    }
}
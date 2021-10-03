package me.steven.indrev.networks.energy

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import team.reborn.energy.api.EnergyStorage

class CableEnergyIo(private val network: EnergyNetwork?) : EnergyStorage, SnapshotParticipant<Long>() {

    override fun getAmount(): Long = network?.energy ?: 0

    override fun getCapacity(): Long = network?.capacity ?: 0

    override fun insert(maxAmount: Long, transaction: TransactionContext?): Long {
        if (network == null) return 0
        val inserted = maxAmount.coerceAtMost(network.maxCableTransfer).coerceAtMost(capacity - amount)
        network.energy += inserted
        return inserted
    }

    override fun extract(maxAmount: Long, transaction: TransactionContext?): Long = 0

    override fun supportsInsertion(): Boolean = true

    override fun supportsExtraction(): Boolean = false

    override fun createSnapshot(): Long {
        return network?.energy ?: 0
    }

    override fun readSnapshot(snapshot: Long) {
        network?.energy = snapshot
    }

    companion object {
        val NO_NETWORK = CableEnergyIo(null)
    }
}
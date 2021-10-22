package me.steven.indrev.networks.energy

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorage
import java.util.*

class CableEnergyIo(private val network: EnergyNetwork?, val pos: BlockPos, val direction: Direction?) : EnergyStorage, SnapshotParticipant<Long>() {

    override fun getAmount(): Long = network?.energy ?: 0

    override fun getCapacity(): Long = network?.capacity ?: 0

    override fun insert(maxAmount: Long, transaction: TransactionContext?): Long {
        if (network == null || direction == null) return 0
        StoragePreconditions.notNegative(maxAmount)
        val inserted = maxAmount.coerceAtMost(network.maxCableTransfer).coerceAtMost(capacity - amount)
        updateSnapshots(transaction)
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
        val NO_NETWORK = CableEnergyIo(null, BlockPos.ORIGIN, null)
    }
}
package me.steven.indrev.components

import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.utils.IRFluidTank
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Direction
import java.util.*

open class FluidComponent(val syncable: () -> Syncable, val limit: Long, val tankCount: Int = 1) :
    CombinedStorage<FluidVariant, IRFluidTank>(mutableListOf()) {

    init {
        parts.addAll((0 until tankCount).map { IRFluidTank(it) { this } })
    }

    var inputTanks = intArrayOf()
    var outputTanks = intArrayOf()

    var unsided = false
    val transferConfig: SideConfiguration = SideConfiguration(ConfigurationType.FLUID)

    private val exposedSides = EnumMap<Direction, ExposedFluidComponent>(Direction::class.java)


    fun getCachedSide(dir: Direction): ExposedFluidComponent {
        val exposed = exposedSides[dir]
        if (exposed == null || (!unsided && exposed.mode != transferConfig[dir])) {
            exposedSides[dir] = ExposedFluidComponent(dir, transferConfig[dir]!!)
        }
        return exposedSides[dir]!!
    }

    open fun getValidTanks(dir: Direction): IntArray =
        if (unsided) IntArray(tankCount) { it }
        else if (transferConfig[dir]!!.input) inputTanks
        else if (transferConfig[dir]!!.output) outputTanks else IntArray(0)

    open fun getTankCapacity(index: Int): Long = limit

    open fun isFluidValidForTank(index: Int, variant: FluidVariant): Boolean = true

    operator fun set(tank: Int, volume: Long) {
        this.parts[tank].amount = volume
    }

    operator fun get(tank: Int): IRFluidTank = this.parts[tank]

    fun toTag(tag: NbtCompound): NbtCompound {
        val tanksTag = NbtCompound()
        parts.forEachIndexed { index, tank ->
            val tankTag = NbtCompound()
            tankTag.put("fluids", tank.toTag())
            tanksTag.put(index.toString(), tankTag)
        }
        tag.put("tanks", tanksTag)
        transferConfig.writeNbt(tag)
        return tag
    }

    fun fromTag(tag: NbtCompound?) {
        val tanksTag = tag?.getCompound("tanks")

        tanksTag?.keys?.forEach { key ->
            val index = key.toInt()
            val tankTag = tanksTag.getCompound(key)
            parts[index].fromTag(tankTag.getCompound("fluids"))
        }

        transferConfig.readNbt(tag)
    }

    inner class ExposedFluidComponent(val dir: Direction, val mode: TransferMode) : CombinedStorage<FluidVariant, IRFluidTank.ExposedIRFluidTank>(mutableListOf()) {

        init {
            parts.addAll(getValidTanks(dir).map { this@FluidComponent.parts[it].exposed })
        }

        override fun insert(resource: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
            return if (unsided || transferConfig[dir]!!.input) {
                super.insert(resource, maxAmount, transaction)
            }
            else 0
        }

        override fun extract(resource: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
            return if (unsided || transferConfig[dir]!!.output)
                super.extract(resource, maxAmount, transaction)
            else 0
        }

        override fun supportsExtraction(): Boolean = this@FluidComponent.supportsExtraction()

        override fun supportsInsertion(): Boolean = this@FluidComponent.supportsInsertion()

        override fun getVersion(): Long = this@FluidComponent.version

        override fun exactView(transaction: TransactionContext?, resource: FluidVariant?): StorageView<FluidVariant>? {
            return super.exactView(transaction, resource)
        }
        override fun iterator(): MutableIterator<StorageView<FluidVariant>> {
            return super.iterator()
        }
    }
}
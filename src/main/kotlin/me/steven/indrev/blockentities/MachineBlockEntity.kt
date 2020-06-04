package me.steven.indrev.blockentities

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.EnergyMovement
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class MachineBlockEntity(val tier: Tier, registry: MachineRegistry) :
    BlockEntity(registry.blockEntityType(tier)), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder,
    Tickable {
    var lastInputFrom: Direction? = null
    val baseBuffer = registry.buffer(tier)
    var energy = 0.0
        set(value) {
            if (world?.isClient == false) sync()
            field = value.coerceAtMost(maxStoredPower).apply { propertyDelegate[0] = this.toInt() }
        }
    private var delegate: PropertyDelegate? = null
        get() = field ?: createDelegate().apply { field = this }

    override fun tick() {
        if (world?.isClient == false)
            EnergyMovement(this, pos).spread(*Direction.values())
    }

    fun takeEnergy(amount: Double): Boolean {
        return if (amount <= energy) {
            energy -= amount
            true
        } else false
    }

    fun addEnergy(amount: Double): Double {
        val added = (maxStoredPower - energy).coerceAtMost(amount)
        energy += added
        return added
    }

    protected abstract fun createDelegate(): PropertyDelegate

    override fun getPropertyDelegate(): PropertyDelegate {
        val delegate = this.delegate!!
        delegate[1] = maxStoredPower.toInt()
        return delegate
    }

    override fun setStored(amount: Double) {
        this.energy = amount
    }

    override fun getMaxStoredPower(): Double = baseBuffer

    @Deprecated("unsupported", level = DeprecationLevel.ERROR)
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    override fun getMaxOutput(side: EnergySide?): Double = tier.io

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    fun getMaxOutput(direction: Direction) = getMaxOutput(EnergySide.fromMinecraft(direction))

    override fun getStored(side: EnergySide?): Double = energy

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        tag.putDouble("Energy", energy)
        return tag
    }
}
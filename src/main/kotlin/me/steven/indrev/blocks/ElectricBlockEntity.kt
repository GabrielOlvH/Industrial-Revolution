package me.steven.indrev.blocks

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class ElectricBlockEntity(type: BlockEntityType<*>) : BlockEntity(type), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder, Tickable {
    var energy = 0.0
        set(value) {
            propertyDelegate[0] = value.toInt()
            field = value
        }
    private var delegate: PropertyDelegate? = null

    override fun tick() {
        if (world?.isClient == true) return

        val block = this.cachedState.block
        if (block !is ElectricBlock) return
        for (direction in Direction.values()) {
            val targetPos = pos.offset(direction)
            block.tryProvideEnergyTo(world, pos, targetPos)
            markDirty()
        }
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

    private fun getOrCreateDelegate(): PropertyDelegate {
        if (delegate == null) {
            delegate = createDelegate()
            return delegate!!
        }
        return delegate!!
    }

    override fun getPropertyDelegate(): PropertyDelegate {
        val delegate = getOrCreateDelegate()
        delegate[1] = maxStoredPower.toInt()
        return delegate
    }

    override fun setStored(amount: Double) {
        this.energy = amount
    }

    override fun getMaxStoredPower(): Double {
        if (world == null) return 0.0
        val block = this.cachedState.block
        if (block is ElectricBlock) return block.maxBuffer
        return 0.0
    }

    abstract fun getMaxInput(): Double

    abstract fun getMaxOutput(): Double

    @Deprecated("unsupported")
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    @Deprecated("use getMaxOutput() instead", ReplaceWith("getMaxOutput()"))
    override fun getMaxOutput(side: EnergySide?): Double = getMaxOutput()

    @Deprecated("use getMaxInput() instead!", ReplaceWith("getMaxInput()"))
    override fun getMaxInput(side: EnergySide?): Double = getMaxInput()

    override fun getStored(side: EnergySide?): Double {
        val direction = EnergySide.fromMinecraft(this.cachedState[ElectricBlock.FACING])
        if (direction == EnergySide.UNKNOWN || direction == side) return 0.0
        return energy
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
        propertyDelegate[0] = energy.toInt()
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        energy = tag?.getDouble("Energy") ?: 0.0
        propertyDelegate[0] = energy.toInt()
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        tag.putDouble("Energy", energy)
        return tag
    }
}
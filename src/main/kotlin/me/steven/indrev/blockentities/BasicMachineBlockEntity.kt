package me.steven.indrev.blockentities

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blocks.BasicMachineBlock
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class BasicMachineBlockEntity(type: BlockEntityType<*>, val baseBuffer: Double) : BlockEntity(type), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder, Tickable {
    var energy = 0.0
        set(value) {
            field = value.coerceAtMost(maxStoredPower)
            propertyDelegate[0] = field.toInt()
        }
        get() {
            field = field.coerceAtMost(maxStoredPower)
            propertyDelegate[0] = field.toInt()
            return field
        }
    private var delegate: PropertyDelegate? = null
        get() {
            if (field == null) field = createDelegate()
            return field
        }

    override fun tick() {
        if (world?.isClient == true) return

        val block = this.cachedState.block
        val handler = Energy.of(this)
        Direction.values()
            .associate { direction ->
                if (block !is BasicMachineBlock || this.getMaxOutput(direction) <= 0) return@associate Pair(null, null)
                val targetPos = pos.offset(direction)
                val target = world?.getBlockEntity(targetPos)
                if (target == null || !Energy.valid(target)) return@associate Pair(null, null)
                val targetHandler = Energy.of(target).side(direction.opposite)
                if (targetHandler.energy >= targetHandler.maxStored) Pair(null, null)
                else Pair(direction, targetHandler)
            }
            .filter { (left, right) -> left != null && right != null }
            .apply {
                val sum = values.sumByDouble { it!!.maxInput }
                forEach { pair ->
                    val targetHandler = pair.value
                    handler.side(pair.key)
                    val targetMaxInput = targetHandler!!.maxInput
                    val weight = (targetMaxInput / sum) * handler.maxOutput
                    if (weight > 0)
                        handler.into(targetHandler).move(weight)
                }
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

    override fun getPropertyDelegate(): PropertyDelegate {
        val delegate = this.delegate!!
        delegate[1] = maxStoredPower.toInt()
        return delegate
    }

    override fun setStored(amount: Double) {
        this.energy = amount
    }

    override fun getMaxStoredPower(): Double = baseBuffer
    @Deprecated("unsupported")
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    abstract override fun getMaxOutput(side: EnergySide?): Double

    fun getMaxOutput(direction: Direction) = getMaxOutput(EnergySide.fromMinecraft(direction))

    abstract override fun getMaxInput(side: EnergySide?): Double

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
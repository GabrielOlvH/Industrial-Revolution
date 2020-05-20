package me.steven.indrev.blocks

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blocks.generators.GeneratorBlock
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.container.PropertyDelegate
import net.minecraft.nbt.CompoundTag
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class ElectricBlockEntity(type: BlockEntityType<*>, private val tier: EnergyTier) : BlockEntity(type), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder {
    var energy = 0.0
    private var delegate: PropertyDelegate? = null

    protected abstract fun createDelegate(): PropertyDelegate

    protected fun getOrCreateDelegate(): PropertyDelegate {
        if (delegate == null) {
            delegate = createDelegate()
            return delegate!!
        }
        return delegate!!
    }

    override fun getPropertyDelegate(): PropertyDelegate {
        val delegate = getOrCreateDelegate()
        delegate[0] = energy.toInt()
        delegate[1] = maxStoredPower.toInt()
        return delegate
    }

    override fun setStored(amount: Double) {
        this.energy = amount
        getOrCreateDelegate()[0] = energy.toInt()
    }

    override fun getMaxStoredPower(): Double {
        val block = this.cachedState.block
        if (block is GeneratorBlock) return block.maxBuffer
        return 0.0
    }

    override fun getTier(): EnergyTier = tier

    override fun getStored(side: EnergySide?): Double {
        val direction = EnergySide.fromMinecraft(this.cachedState[ElectricBlock.FACING])
        if (direction == EnergySide.UNKNOWN || direction == side) return 0.0
        return energy
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
        getOrCreateDelegate()[0] = energy.toInt()
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        energy = tag?.getDouble("Energy") ?: 0.0
        getOrCreateDelegate()[0] = energy.toInt()
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        tag.putDouble("Energy", energy)
        return tag
    }


}
package me.steven.indrev.blockentities

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.Property
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EnergyMovement
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.container.PropertyDelegate
import net.minecraft.inventory.SidedInventory
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.IWorld
import net.minecraft.world.explosion.Explosion
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier

abstract class MachineBlockEntity(val tier: Tier, registry: MachineRegistry) :
    BlockEntity(registry.blockEntityType(tier)), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder, InventoryProvider,
    Tickable {
    var lastInputFrom: Direction? = null
    val baseBuffer = registry.buffer(tier)
    var energy: Double by Property(0, 0.0) { i -> i.coerceAtMost(maxStoredPower) }
    var inventoryController: InventoryController? = null
    var temperatureController: TemperatureController? = null
    var explode = false
    private var delegate: PropertyDelegate? = null
        get() = field ?: createDelegate().apply { field = this }

    override fun tick() {
        if (world?.isClient == false) {
            EnergyMovement(this, pos).spread(*Direction.values())
            if (explode) {
                val power = temperatureController!!.explosionPower
                world?.createExplosion(
                    null,
                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                    power,
                    false,
                    Explosion.DestructionType.DESTROY)
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

    @Deprecated("unsupported", level = DeprecationLevel.ERROR)
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    override fun getMaxOutput(side: EnergySide?): Double = tier.io

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    fun getMaxOutput(direction: Direction) = getMaxOutput(EnergySide.fromMinecraft(direction))

    override fun getStored(side: EnergySide?): Double = energy

    override fun getInventory(state: BlockState?, world: IWorld?, pos: BlockPos?): SidedInventory {
        return inventoryController?.getInventory()
            ?: throw IllegalStateException("retrieving inventory from machine without inventory controller!")
    }

    override fun fromTag(tag: CompoundTag?) {
        super.fromTag(tag)
        inventoryController?.fromTag(tag)
        temperatureController?.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
        if (tag != null) {
            inventoryController?.toTag(tag)
            temperatureController?.toTag(tag)
        }
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        inventoryController?.fromTag(tag)
        temperatureController?.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        inventoryController?.toTag(tag)
        temperatureController?.toTag(tag)
        tag.putDouble("Energy", energy)
        return tag
    }
}
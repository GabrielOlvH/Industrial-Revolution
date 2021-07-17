package me.steven.indrev.blockentities

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.machines.properties.Property
import me.steven.indrev.api.sideconfigs.Configurable
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.api.sideconfigs.SideConfiguration
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ComponentProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.transferEnergy
import me.steven.indrev.utils.transferFluids
import me.steven.indrev.utils.transferItems
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.InventoryProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.inventory.SidedInventory
import net.minecraft.nbt.NbtCompound
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldAccess
import kotlin.collections.set
import kotlin.math.roundToInt

abstract class MachineBlockEntity<T : IConfig>(val tier: Tier, val registry: MachineRegistry, pos: BlockPos, state: BlockState)
    : IRSyncableBlockEntity(registry.blockEntityType(tier), pos, state), PropertyDelegateHolder, InventoryProvider, EnergyIo,
    Configurable, ComponentProvider {

    val validConnections = mutableSetOf<Direction>().also { it.addAll(Direction.values()) }

    private var propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(4)

    private var lastEnergyUpdate = 0

    internal var energy: Double by Property(0, 0.0) { i ->
        when {
            i.isNaN() -> {
                IndustrialRevolution.LOGGER.error("Received NaN energy! $pos, $this", IllegalArgumentException())
                if (energy.isNaN()) 0.0 else energy
            }
            tier == Tier.CREATIVE -> energyCapacity
            else -> i.coerceIn(0.0, energyCapacity)
        }
    }
    open val maxInput: Double = tier.io
    open val maxOutput: Double = tier.io

    var inventoryComponent: InventoryComponent? = null
    var temperatureComponent: TemperatureComponent? = null
    var fluidComponent: FluidComponent? = null
    var multiblockComponent: MultiBlockComponent? = null

    var itemTransferCooldown = 0

    var workingState: Boolean = false
        set(value) {
            val update = value != field
            field = value
            if (update && world?.isClient == false)
                GlobalStateController.update(world!!, pos, value)
        }
        get() {
            if (world?.isClient == true && GlobalStateController.workingStateTracker.contains(pos.asLong())) {
                MinecraftClient.getInstance().execute { field = GlobalStateController.workingStateTracker.remove(pos.asLong()) }
            }
            return field
        }

    var ticks = 0

    @Suppress("UNCHECKED_CAST")
    val config: T by lazy { registry.config(tier) as T }

    protected open fun machineTick() {}

    @Environment(EnvType.CLIENT)
    open fun machineClientTick() {}

    fun tick() {
        if (this is EnhancerProvider && inventoryComponent != null)
            this.updateEnhancers(inventoryComponent!!.inventory)
        ticks++
        propertyDelegate[1] = energyCapacity.toInt()
        multiblockComponent?.tick(world!!, pos, cachedState)
        if (multiblockComponent?.isBuilt(world!!, pos, cachedState) == false) return

        transferEnergy()
        transferItems()
        transferFluids()

        machineTick()
        if (isMarkedForUpdate) {
            lastEnergyUpdate = energy.roundToInt()
            markDirty()
            sync()
            isMarkedForUpdate = false
        }
    }

    override fun getEnergy(): Double = if (tier == Tier.CREATIVE) energyCapacity else energy

    override fun getEnergyCapacity(): Double = config.maxEnergyStored

    open fun getEnergyCost(): Double = 0.0

    override fun getPropertyDelegate(): PropertyDelegate = propertyDelegate

    fun setPropertyDelegate(propertyDelegate: PropertyDelegate) {
        this.propertyDelegate = propertyDelegate
    }

    override fun insert(amount: Double, simulation: Simulation?): Double {
        val inserted = amount.coerceAtMost(this.maxInput).coerceAtMost(this.energyCapacity - energy)
        if (simulation?.isActing == true) this.energy += inserted
        return amount - inserted
    }

    override fun extract(maxAmount: Double, simulation: Simulation?): Double {
        val extracted = maxAmount.coerceAtMost(this.maxOutput).coerceAtMost(energy)
        if (simulation?.isActing == true) this.energy -= extracted
        return extracted
    }

    override fun supportsExtraction(): Boolean = maxOutput > 0

    override fun supportsInsertion(): Boolean = maxInput > 0

    // internal consumption
    fun use(amount: Double): Boolean {
        val extracted = amount.coerceAtMost(energy)
        if (extracted == amount) {
            this.energy -= extracted
            return true
        }
        return false
    }

    fun canUse(amount: Double): Boolean {
        val extracted = amount.coerceAtMost(energy)
        return extracted == amount
    }

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory? = inventoryComponent?.inventory

    override fun isConfigurable(type: ConfigurationType): Boolean {
        return when (type) {
            ConfigurationType.ITEM -> inventoryComponent != null
                    && (inventoryComponent?.inventory?.inputSlots?.isNotEmpty() == true
                    || inventoryComponent?.inventory?.outputSlots?.isNotEmpty() == true)
            ConfigurationType.FLUID -> fluidComponent != null
            ConfigurationType.ENERGY -> false
        }
    }

    override fun applyDefault(state: BlockState, type: ConfigurationType, configuration: MutableMap<Direction, TransferMode>) {
        val direction = (state.block as MachineBlock).getFacing(state)
        when (type) {
            ConfigurationType.ITEM -> {
                configuration[direction.rotateYClockwise()] = TransferMode.INPUT
                configuration[direction.rotateYCounterclockwise()] = TransferMode.OUTPUT
            }
            ConfigurationType.ENERGY -> throw IllegalArgumentException("cannot apply energy configuration to $this")
            else -> return
        }
    }

    override fun getValidConfigurations(type: ConfigurationType): Array<TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> TransferMode.DEFAULT
            ConfigurationType.FLUID, ConfigurationType.ENERGY -> arrayOf(TransferMode.INPUT, TransferMode.OUTPUT, TransferMode.NONE)
        }
    }

    override fun getCurrentConfiguration(type: ConfigurationType): SideConfiguration {
        return when (type) {
            ConfigurationType.ITEM -> inventoryComponent!!.itemConfig
            ConfigurationType.FLUID -> fluidComponent!!.transferConfig
            ConfigurationType.ENERGY -> error("nope")
        }
    }

    override fun isFixed(type: ConfigurationType): Boolean = false

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.FLUID -> fluidComponent
            ComponentKey.ITEM -> inventoryComponent
            ComponentKey.TEMPERATURE -> temperatureComponent
            ComponentKey.MULTIBLOCK -> multiblockComponent
            ComponentKey.PROPERTY_HOLDER -> this
            else -> null
        }
    }

    open fun getFluidTransferRate(): FluidAmount = when (tier) {
        Tier.MK1 -> FluidAmount.BOTTLE
        Tier.MK2 -> FluidAmount.BOTTLE.mul(2)
        else -> FluidAmount.BUCKET
    }

    override fun readNbt(tag: NbtCompound?) {
        super.readNbt(tag)
        inventoryComponent?.readNbt(tag)
        temperatureComponent?.readNbt(tag)
        fluidComponent?.fromTag(tag)
        multiblockComponent?.readNbt(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun writeNbt(tag: NbtCompound?): NbtCompound {
        tag?.putDouble("Energy", energy)
        if (tag != null) {
            inventoryComponent?.writeNbt(tag)
            temperatureComponent?.writeNbt(tag)
            fluidComponent?.toTag(tag)
            multiblockComponent?.writeNbt(tag)
        }
        return super.writeNbt(tag)
    }

    override fun fromClientTag(tag: NbtCompound?) {
        inventoryComponent?.readNbt(tag)
        temperatureComponent?.readNbt(tag)
        fluidComponent?.fromTag(tag)
        multiblockComponent?.readNbt(tag)
        workingState = tag?.getBoolean("WorkingState") ?: workingState
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toClientTag(tag: NbtCompound?): NbtCompound {
        if (tag == null) return NbtCompound()
        inventoryComponent?.writeNbt(tag)
        temperatureComponent?.writeNbt(tag)
        fluidComponent?.toTag(tag)
        multiblockComponent?.writeNbt(tag)
        tag.putDouble("Energy", energy)
        tag.putBoolean("WorkingState", workingState)
        return tag
    }
}
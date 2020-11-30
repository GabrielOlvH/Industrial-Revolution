package me.steven.indrev.blockentities

import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidExtractable
import alexiil.mc.lib.attributes.fluid.FluidInsertable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemInvUtil
import alexiil.mc.lib.attributes.item.compat.FixedSidedInventoryVanillaWrapper
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.api.sideconfigs.Configurable
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.components.TemperatureComponent
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.components.multiblock.MultiBlockComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.energy.EnergyMovement
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.screen.PropertyDelegate
import net.minecraft.util.Tickable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.WorldAccess
import net.minecraft.world.explosion.Explosion
import org.apache.logging.log4j.LogManager
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class MachineBlockEntity<T : IConfig>(val tier: Tier, val registry: MachineRegistry)
    : IRSyncableBlockEntity(registry.blockEntityType(tier)), EnergyStorage, PropertyDelegateHolder, InventoryProvider, Tickable,
    Configurable {
    var explode = false
    private var propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(4)

    private var lastEnergyUpdate = 0

    var energy: Double by Property(0, 0.0) { i -> i.coerceIn(0.0, maxStoredPower) }
    var inventoryComponent: InventoryComponent? = null
    var temperatureComponent: TemperatureComponent? = null
    var fluidComponent: FluidComponent? = null
    var multiblockComponent: MultiBlockComponent? = null

    var itemTransferCooldown = 0

    var workingState: Boolean = false

    var ticks = 0

    val config: T by lazy { registry.config(tier) as T }

    protected open fun machineTick() {}

    final override fun tick() {
        if (world?.isClient == false) {
            ticks++
            multiblockComponent?.tick(world!!, pos, cachedState)
            if (multiblockComponent?.isBuilt(world!!, pos, cachedState) == false) return
            EnergyMovement.spreadNeighbors(this, pos)
            if (explode) {
                val power = temperatureComponent!!.explosionPower
                world?.createExplosion(
                    null,
                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                    power,
                    false,
                    Explosion.DestructionType.DESTROY)
                LogManager.getLogger("Industrial Revolution")
                    .debug("Exploded machine $this with temperature ${this.temperatureComponent?.temperature}")
                return
            }
            val inventory = inventoryComponent?.inventory
            if (inventoryComponent != null && inventory!!.size() > 0) {
                val stack = inventory.getStack(0)
                if (Energy.valid(stack))
                    Energy.of(stack).into(Energy.of(this)).move()
            }
            transferItems()
            transferFluids()
            machineTick()
            if (isMarkedForUpdate) {
                lastEnergyUpdate = energy.roundToInt()
                markDirty()
                sync()
                isMarkedForUpdate = false
            }
            if (ticks % 20 == 0
                && this.cachedState.contains(MachineBlock.WORKING_PROPERTY)
                && this.cachedState[MachineBlock.WORKING_PROPERTY] != workingState
            ) {
                    val state = this.cachedState.with(MachineBlock.WORKING_PROPERTY, workingState)
                    world!!.setBlockState(pos, state)
            }
        }
    }

    override fun getPropertyDelegate(): PropertyDelegate {
        val delegate = this.propertyDelegate
        delegate[1] = maxStoredPower.toInt()
        return delegate
    }

    fun setPropertyDelegate(propertyDelegate: PropertyDelegate) {
        this.propertyDelegate = propertyDelegate
    }

    override fun setStored(amount: Double) {
        markForUpdate {
            val abs = abs(lastEnergyUpdate - amount)
            val max = (0.01 * maxStoredPower).coerceAtLeast(1.0)
            this.tier != Tier.CREATIVE && abs > max
        }
        this.energy = amount
    }

    open fun getBaseBuffer(): Double = config.maxEnergyStored

    override fun getMaxStoredPower(): Double = getBaseBuffer()

    @Deprecated("unsupported", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("this.tier"))
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    override fun getMaxOutput(side: EnergySide?): Double = tier.io

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    fun getMaxOutput(direction: Direction) = getMaxOutput(EnergySide.fromMinecraft(direction))

    override fun getStored(side: EnergySide?): Double = if (tier != Tier.CREATIVE) energy else maxStoredPower

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory? = inventoryComponent?.inventory

    override fun isConfigurable(type: ConfigurationType): Boolean {
        return when (type) {
            ConfigurationType.ITEM -> inventoryComponent != null
                    && inventoryComponent?.inventory?.inputSlots?.isNotEmpty() == true
                    && inventoryComponent?.inventory?.outputSlots?.isNotEmpty() == true
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
            ConfigurationType.ITEM -> TransferMode.values()
            ConfigurationType.FLUID, ConfigurationType.ENERGY -> arrayOf(TransferMode.INPUT, TransferMode.OUTPUT)
        }
    }

    override fun getCurrentConfiguration(type: ConfigurationType): MutableMap<Direction, TransferMode> {
        return when (type) {
            ConfigurationType.ITEM -> inventoryComponent!!.itemConfig
            ConfigurationType.FLUID -> fluidComponent!!.transferConfig
            ConfigurationType.ENERGY -> hashMapOf()
        }
    }

    override fun isFixed(type: ConfigurationType): Boolean = false

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        inventoryComponent?.fromTag(tag)
        temperatureComponent?.fromTag(tag)
        fluidComponent?.fromTag(tag)
        multiblockComponent?.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
        if (tag != null) {
            inventoryComponent?.toTag(tag)
            temperatureComponent?.toTag(tag)
            fluidComponent?.toTag(tag)
            multiblockComponent?.toTag(tag)
        }
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        inventoryComponent?.fromTag(tag)
        temperatureComponent?.fromTag(tag)
        fluidComponent?.fromTag(tag)
        multiblockComponent?.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        inventoryComponent?.toTag(tag)
        temperatureComponent?.toTag(tag)
        fluidComponent?.toTag(tag)
        multiblockComponent?.toTag(tag)
        tag.putDouble("Energy", energy)
        return tag
    }

    private fun transferItems() {
        itemTransferCooldown--
        if (itemTransferCooldown <= 0) {
            itemTransferCooldown = 0
            inventoryComponent?.itemConfig?.forEach { (direction, mode) ->
                val pos = pos.offset(direction)
                val inventory = inventoryComponent?.inventory ?: return@forEach
                if (mode.output) {
                    val neighborInv = getInventory(pos)
                    if (neighborInv != null) {
                        inventory.outputSlots.forEach { slot ->
                            transferItems(inventory, neighborInv, slot, direction)
                        }
                        return@forEach
                    }
                    val insertable = ItemAttributes.INSERTABLE.getFirstOrNull(world, pos)
                    if (insertable != null) {
                        val extractable = FixedSidedInventoryVanillaWrapper.create(inventory, direction).extractable
                        ItemInvUtil.move(extractable, insertable, 64)
                    }
                }
                if (mode.input) {
                    val neighborInv = getInventory(pos)
                    if (neighborInv != null) {
                        getAvailableSlots(neighborInv, direction.opposite).forEach { slot ->
                            transferItems(neighborInv, inventory, slot, direction.opposite)
                        }
                        return@forEach
                    }
                    val extractable = ItemAttributes.EXTRACTABLE.getFirstOrNull(world, pos)
                    if (extractable != null) {
                        val insertable = FixedSidedInventoryVanillaWrapper.create(inventory, direction).insertable
                        ItemInvUtil.move(extractable, insertable, 64)
                    }
                }
            }
        }
    }

    private fun getFirstSlot(inventory: Inventory, predicate: (Int, ItemStack) -> Boolean): Int? =
        (0 until inventory.size()).firstOrNull { slot -> predicate(slot, inventory.getStack(slot)) }

    private fun transferItems(from: Inventory, to: Inventory, slot: Int, direction: Direction) {
        val toTransfer = from.getStack(slot)
        while (!toTransfer.isEmpty) {
            val firstSlot = getFirstSlot(to) { firstSlot, firstStack ->
                (canMergeItems(firstStack, toTransfer) || firstStack.isEmpty)
                    && (to !is SidedInventory || to.canInsert(firstSlot, toTransfer, direction.opposite))
            } ?: break
            val targetStack = to.getStack(firstSlot)
            if (from is SidedInventory && !from.canExtract(slot, toTransfer, direction))
                break
            val availableSize = (toTransfer.maxCount - targetStack.count).coerceAtMost(toTransfer.count)
            if (!targetStack.isEmpty) {
                toTransfer.count -= availableSize
                targetStack.count += availableSize
            } else {
                from.setStack(slot, ItemStack.EMPTY)
                to.setStack(firstSlot, toTransfer)
                break
            }
            itemTransferCooldown = 12
        }
    }

    private fun getAvailableSlots(inventory: Inventory, side: Direction): IntArray =
        if (inventory is SidedInventory) inventory.getAvailableSlots(side)
        else (0 until inventory.size()).toIntArray()

    private fun canMergeItems(first: ItemStack, second: ItemStack): Boolean =
        first.item == second.item
            && first.damage == second.damage
            && first.count < first.maxCount
            && ItemStack.areTagsEqual(first, second)

    private fun getInventory(pos: BlockPos): Inventory? {
        val blockState = world?.getBlockState(pos)
        val block = blockState?.block
        return when {
            block is InventoryProvider -> block.getInventory(blockState, world, pos)
            block?.hasBlockEntity() == true -> {
                val blockEntity = world?.getBlockEntity(pos) as? Inventory ?: return null
                if (blockEntity is ChestBlockEntity && block is ChestBlock)
                    ChestBlock.getInventory(block, blockState, world, pos, true)
                else blockEntity
            }
            else -> null
        }
    }

    private fun transferFluids() {
        fluidComponent?.tanks?.forEach { tank ->
            fluidComponent?.transferConfig?.forEach innerForEach@{ (direction, mode) ->
                if (mode == TransferMode.NONE) return@innerForEach
                var extractable: FluidExtractable? = null
                var insertable: FluidInsertable? = null
                if (mode.output) {
                    insertable = FluidAttributes.INSERTABLE.getAllFromNeighbour(this, direction).firstOrNull
                        ?: return@innerForEach
                    extractable = fluidComponent?.extractable
                }
                if (mode.input) {
                    extractable = FluidAttributes.EXTRACTABLE.getAllFromNeighbour(this, direction).firstOrNull ?: return@innerForEach
                    insertable = fluidComponent?.insertable

                }
                if (extractable != null && insertable != null)
                    FluidVolumeUtil.move(extractable, insertable, NUGGET_AMOUNT)
            }
        }
    }
}
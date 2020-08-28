package me.steven.indrev.blockentities

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.components.*
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EnergyMovement
import me.steven.indrev.utils.NUGGET_AMOUNT
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.toIntArray
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.BlockEntity
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

abstract class MachineBlockEntity(val tier: Tier, val registry: MachineRegistry)
    : BlockEntity(registry.blockEntityType(tier)), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder, InventoryProvider, Tickable {
    var explode = false
    private var propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(3)

    var energy: Double by Property(0, 0.0) { i -> i.coerceIn(0.0, maxStoredPower) }
    var inventoryComponent: InventoryComponent? = null
    var temperatureComponent: TemperatureComponent? = null
    var fluidComponent: FluidComponent? = null

    var itemTransferCooldown = 0

    protected open fun machineTick() {}

    override fun tick() {
        if (world?.isClient == false) {
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
            markDirty()
            sync()
        }
    }

    protected fun setWorkingState(value: Boolean) {
        if (world?.isClient == false && this.cachedState.contains(MachineBlock.WORKING_PROPERTY) && this.cachedState[MachineBlock.WORKING_PROPERTY] != value) {
            val state = this.cachedState.with(MachineBlock.WORKING_PROPERTY, value)
            world!!.setBlockState(pos, state)
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
        this.energy = amount
    }

    abstract fun getBaseBuffer(): Double

    override fun getMaxStoredPower(): Double = getBaseBuffer()

    @Deprecated("unsupported", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("this.tier"))
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    override fun getMaxOutput(side: EnergySide?): Double = tier.io

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    fun getMaxOutput(direction: Direction) = getMaxOutput(EnergySide.fromMinecraft(direction))

    override fun getStored(side: EnergySide?): Double = if (tier != Tier.CREATIVE) energy else maxStoredPower

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory? {
        return inventoryComponent?.inventory
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        inventoryComponent?.fromTag(tag)
        temperatureComponent?.fromTag(tag)
        fluidComponent?.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("Energy", energy)
        if (tag != null) {
            inventoryComponent?.toTag(tag)
            temperatureComponent?.toTag(tag)
            fluidComponent?.toTag(tag)
        }
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        inventoryComponent?.fromTag(tag)
        temperatureComponent?.fromTag(tag)
        fluidComponent?.fromTag(tag)
        energy = tag?.getDouble("Energy") ?: 0.0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        if (tag == null) return CompoundTag()
        inventoryComponent?.toTag(tag)
        temperatureComponent?.toTag(tag)
        fluidComponent?.toTag(tag)
        tag.putDouble("Energy", energy)
        return tag
    }

    private fun transferItems() {
        itemTransferCooldown--
        inventoryComponent?.itemConfig?.forEach { (direction, mode) ->
            val pos = pos.offset(direction)
            val neighborInv = getInventory(pos) ?: return@forEach
            val inventory = inventoryComponent?.inventory ?: return@forEach
            if (mode.output) {
                inventory.outputSlots.forEach { slot ->
                    transferItems(inventory, neighborInv, slot, direction)
                }
            }
            if (mode.input) {
                getAvailableSlots(neighborInv, direction.opposite).forEach { slot ->
                    transferItems(neighborInv, inventory, slot, direction.opposite)
                }
            }
        }
    }

    private fun getFirstSlot(inventory: Inventory, predicate: (Int, ItemStack) -> Boolean): Int? =
        (0 until inventory.size()).firstOrNull { slot -> predicate(slot, inventory.getStack(slot)) }

    private fun transferItems(from: Inventory, to: Inventory, slot: Int, direction: Direction) {
        if (itemTransferCooldown > 0) return
        val toTransfer = from.getStack(slot)
        while (!toTransfer.isEmpty) {
            val firstSlot = getFirstSlot(to) { firstSlot, firstStack ->
                (canMergeItems(firstStack, toTransfer) || firstStack.isEmpty)
                    && (to !is SidedInventory || to.canInsert(firstSlot, firstStack, direction.opposite))
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
                if (mode.output) {
                    val fluidAmount =
                        (if (tank.volume.amount()?.compareTo(NUGGET_AMOUNT) ?: return@innerForEach > 0)
                            NUGGET_AMOUNT
                        else
                            tank.volume.amount()) ?: return@innerForEach
                    val insertable = FluidAttributes.INSERTABLE.getAllFromNeighbour(this, direction).firstOrNull
                        ?: return@innerForEach
                    val extractable = fluidComponent?.extractable
                    val extractionResult = extractable?.attemptAnyExtraction(fluidAmount, Simulation.SIMULATE)
                    val insertionResult = insertable.attemptInsertion(extractionResult, Simulation.SIMULATE)
                    if (extractionResult?.isEmpty == false && insertionResult.isEmpty) {
                        insertable.insert(extractionResult)
                        extractable.extract(extractionResult.amount())
                    }
                }
                if (mode.input) {
                    val extractable = FluidAttributes.EXTRACTABLE.getAllFromNeighbour(this, direction).firstOrNull
                        ?: return@innerForEach
                    val fluidAmount = NUGGET_AMOUNT
                    val insertable = fluidComponent?.insertable
                    val extractionResult = extractable.attemptAnyExtraction(fluidAmount, Simulation.SIMULATE)
                    val insertionResult = insertable?.attemptInsertion(extractionResult, Simulation.SIMULATE)
                    if (insertionResult?.isEmpty == true && !extractionResult.isEmpty) {
                        extractable.extract(extractionResult?.amount())
                        insertable.insert(extractionResult)
                    }
                }
            }
        }
    }
}
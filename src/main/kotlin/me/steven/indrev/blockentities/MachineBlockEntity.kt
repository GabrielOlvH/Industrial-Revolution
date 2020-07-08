package me.steven.indrev.blockentities

import io.github.cottonmc.cotton.gui.PropertyDelegateHolder
import me.steven.indrev.blocks.MachineBlock
import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.Property
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.EnergyMovement
import me.steven.indrev.utils.Tier
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
import net.minecraft.util.registry.Registry
import net.minecraft.world.WorldAccess
import net.minecraft.world.explosion.Explosion
import team.reborn.energy.EnergySide
import team.reborn.energy.EnergyStorage
import team.reborn.energy.EnergyTier
import java.util.*
import java.util.stream.IntStream

open class MachineBlockEntity(val tier: Tier, val registry: MachineRegistry)
    : BlockEntity(registry.blockEntityType(tier)), BlockEntityClientSerializable, EnergyStorage, PropertyDelegateHolder, InventoryProvider, Tickable {
    private val typeId = Registry.BLOCK_ENTITY_TYPE.getRawId(type)
    var viewers = mutableMapOf<UUID, Int>()

    var explode = false
    private var propertyDelegate: PropertyDelegate = ArrayPropertyDelegate(3)

    var energy: Double by Property(0, 0.0) { i -> i.coerceAtMost(maxStoredPower).coerceAtLeast(0.0) }
    var inventoryController: InventoryController? = null
    var temperatureController: TemperatureController? = null

    var itemTransferCooldown = 0

    protected open fun machineTick() {}

    override fun tick() {
        if (world?.isClient == false) {
            EnergyMovement.spreadNeighbors(this, pos)
            if (explode) {
                val power = temperatureController!!.explosionPower
                world?.createExplosion(
                    null,
                    pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(),
                    power,
                    false,
                    Explosion.DestructionType.DESTROY)
            }
            itemTransferCooldown--
            inventoryController?.itemConfig?.forEach { (direction, mode) ->
                val pos = pos.offset(direction)
                val neighborInv = getInventory(pos)
                if (mode.output) {
                    if (neighborInv != null) {
                        inventoryController?.inventory?.outputSlots?.forEach { slot ->
                            insertAndExtract(inventoryController!!.inventory, neighborInv, direction) {
                                extract(inventoryController!!.inventory, slot, direction)
                            }
                        }
                    }
                } else if (mode.input) {
                    if (neighborInv != null) {
                        getAvailableSlots(neighborInv, direction).forEach { slot ->
                            insertAndExtract(neighborInv, inventoryController!!.inventory, direction.opposite) {
                                extract(neighborInv, slot, direction)
                            }
                        }
                    }
                }
            }
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

    protected fun getBaseBuffer() = registry.buffer(tier)

    override fun getMaxStoredPower(): Double = getBaseBuffer()

    @Deprecated("unsupported", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("this.tier"))
    override fun getTier(): EnergyTier = throw UnsupportedOperationException()

    override fun getMaxOutput(side: EnergySide?): Double = tier.io

    override fun getMaxInput(side: EnergySide?): Double = tier.io

    fun getMaxOutput(direction: Direction) = getMaxOutput(EnergySide.fromMinecraft(direction))

    override fun getStored(side: EnergySide?): Double = if (tier != Tier.CREATIVE) energy else maxStoredPower

    override fun getInventory(state: BlockState?, world: WorldAccess?, pos: BlockPos?): SidedInventory {
        return inventoryController?.inventory
            ?: throw IllegalStateException("retrieving inventory from machine without inventory controller!")
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
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

    private fun needsCooldown() = itemTransferCooldown > 0


    private fun insertAndExtract(from: Inventory, to: Inventory, direction: Direction, extractMethod: () -> Boolean): Boolean {

        return if (world != null && !world!!.isClient && !this.needsCooldown()) {
            if (!from.isEmpty && !insert(from, to, direction)) return false
            if (!isFull(to) && !extractMethod()) return false
            this.itemTransferCooldown = 8
            markDirty()
            return true
        } else false
    }

    private fun isFull(inv: Inventory): Boolean {
        for (slot in 0 until inv.size()) if (!inv.getStack(slot).isEmpty) return false
        return true
    }

    private fun insert(from: Inventory, to: Inventory, direction: Direction): Boolean {
        val opposite = direction.opposite
        return if (!isInventoryFull(to, opposite)) {
            for (slot in 0 until from.size()) {
                val stack = from.getStack(slot)
                if (!stack.isEmpty && (from !is SidedInventory || from.canExtract(slot, stack, direction))) {
                    val itemStack = stack.copy()
                    val remainder = transfer(to, from.removeStack(slot, 1), opposite)
                    if (remainder.isEmpty) {
                        to.markDirty()
                        return true
                    }
                    from.setStack(slot, itemStack)
                }
            }
            false
        } else false
    }

    private fun getAvailableSlots(inventory: Inventory, side: Direction): IntStream {
        return if (inventory is SidedInventory) IntStream.of(*inventory.getAvailableSlots(side)) else IntStream.range(0, inventory.size())
    }

    private fun isInventoryFull(inv: Inventory, direction: Direction): Boolean {
        return getAvailableSlots(inv, direction).allMatch { slot ->
            val itemStack = inv.getStack(slot)
            itemStack.count >= itemStack.maxCount
        }
    }

    private fun isInventoryEmpty(inv: Inventory, facing: Direction): Boolean {
        return getAvailableSlots(inv, facing).allMatch { slot -> inv.getStack(slot).isEmpty }
    }

    private fun extract(inventory: Inventory, slot: Int, side: Direction): Boolean {
        val itemStack = inventory.getStack(slot)
        if (!itemStack.isEmpty && canExtract(inventory, itemStack, slot, side)) {
            val copy = itemStack.copy()
            val remainder = transfer(inventory, inventory.removeStack(slot, 1), null)
            if (remainder.isEmpty) {
                inventory.markDirty()
                return true
            }
            inventory.setStack(slot, copy)
        }
        return false
    }

    open fun transfer(to: Inventory, stack: ItemStack, side: Direction?): ItemStack {
        var stack = stack
        if (to is SidedInventory && side != null) {
            val availableSlots = to.getAvailableSlots(side)
            var slot = 0
            while (slot < availableSlots.size && !stack.isEmpty) {
                stack = transfer(to, stack, availableSlots[slot], side)
                ++slot
            }
        } else {
            var slot = 0
            while (slot < to.size() && !stack.isEmpty) {
                stack = transfer(to, stack, slot, side)
                ++slot
            }
        }
        return stack
    }

    private fun canInsert(inventory: Inventory, stack: ItemStack, slot: Int, side: Direction?): Boolean =
        inventory.isValid(slot, stack)
            && (inventory !is SidedInventory
            || inventory.canInsert(slot, stack, side))

    private fun canExtract(inv: Inventory, stack: ItemStack, slot: Int, facing: Direction): Boolean {
        return inv !is SidedInventory || inv.canExtract(slot, stack, facing)
    }

    private fun transfer(to: Inventory, stack: ItemStack, slot: Int, direction: Direction?): ItemStack {
        var stack = stack
        val itemStack = to.getStack(slot)
        if (canInsert(to, stack, slot, direction)) {
            if (itemStack.isEmpty) {
                to.setStack(slot, stack)
                stack = ItemStack.EMPTY
            } else if (canMergeItems(itemStack, stack)) {
                val amount = stack.count.coerceAtMost(stack.maxCount - itemStack.count)
                stack.decrement(amount)
                itemStack.increment(amount)
            }
        }
        return stack
    }


    private fun canMergeItems(first: ItemStack, second: ItemStack): Boolean = first.item == second.item && first.damage == second.damage && first.count < first.maxCount && ItemStack.areTagsEqual(first, second)

    private fun getInventory(pos: BlockPos): Inventory? {
        val blockState = world?.getBlockState(pos)
        val block = blockState?.block
        if (block is InventoryProvider) {
            return block.getInventory(blockState, world, pos)
        } else if (block?.hasBlockEntity() == true) {
            val blockEntity = world?.getBlockEntity(pos)
            if (blockEntity is Inventory) {
                if (blockEntity is ChestBlockEntity && block is ChestBlock)
                    return ChestBlock.getInventory(block, blockState, world, pos, true)
                return blockEntity
            }
        }
        return null
    }
}
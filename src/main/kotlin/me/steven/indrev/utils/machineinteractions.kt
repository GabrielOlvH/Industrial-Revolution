package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.FluidExtractable
import alexiil.mc.lib.attributes.fluid.FluidInsertable
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.item.ItemAttributes
import alexiil.mc.lib.attributes.item.ItemInvUtil
import alexiil.mc.lib.attributes.item.compat.FixedSidedInventoryVanillaWrapper
import dev.technici4n.fasttransferlib.api.energy.EnergyMovement
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.blockentities.MachineBlockEntity
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.minecraft.block.ChestBlock
import net.minecraft.block.InventoryProvider
import net.minecraft.block.entity.ChestBlockEntity
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SidedInventory
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

fun MachineBlockEntity<*>.transferItems() {
    itemTransferCooldown--
    if (itemTransferCooldown <= 0) {
        itemTransferCooldown = 0
        inventoryComponent?.itemConfig?.forEach { (direction, mode) ->
            val pos = pos.offset(direction)
            val inventory = inventoryComponent?.inventory ?: return@forEach
            if (mode.output && inventoryComponent!!.itemConfig.autoPush) {
                val neighborInv = getInvAt(world!!, pos)
                if (neighborInv != null) {
                    inventory.outputSlots.forEach { slot ->
                        transferItems(inventory, neighborInv, slot, direction)
                    }
                    return@forEach
                }
                val insertable = ItemAttributes.INSERTABLE.getFromNeighbour(this, direction)
                val extractable = FixedSidedInventoryVanillaWrapper.create(inventory, direction).extractable
                ItemInvUtil.move(extractable, insertable, 64)
            }
            if (mode.input && inventoryComponent!!.itemConfig.autoPull) {
                val neighborInv = getInvAt(world!!, pos)
                if (neighborInv != null) {
                    getAvailableSlots(neighborInv, direction.opposite).forEach { slot ->
                        transferItems(neighborInv, inventory, slot, direction.opposite)
                    }
                    return@forEach
                }
                val extractable = ItemAttributes.EXTRACTABLE.getFromNeighbour(this, direction)
                val insertable = FixedSidedInventoryVanillaWrapper.create(inventory, direction).insertable
                ItemInvUtil.move(extractable, insertable, 64)
            }
        }
    }
}

fun MachineBlockEntity<*>.transferItems(from: Inventory, to: Inventory, slot: Int, direction: Direction) {
    val toTransfer = from.getStack(slot)
    while (!toTransfer.isEmpty) {
        val firstSlot = (0 until to.size()).firstOrNull { firstSlot ->
            val firstStack = to.getStack(firstSlot)
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
    else (0 until inventory.size()).map { it }.toIntArray()

private fun canMergeItems(first: ItemStack, second: ItemStack): Boolean =
    first.item == second.item
            && first.damage == second.damage
            && first.count < first.maxCount
            && ItemStack.areNbtEqual(first, second)

private fun getInvAt(world: World, pos: BlockPos): Inventory? {
    val blockState = world.getBlockState(pos)
    val block = blockState?.block
    return when {
        block is InventoryProvider -> block.getInventory(blockState, world, pos)
        blockState?.hasBlockEntity() == true -> {
            val blockEntity = world.getBlockEntity(pos) as? Inventory ?: return null
            if (blockEntity is ChestBlockEntity && block is ChestBlock)
                ChestBlock.getInventory(block, blockState, world, pos, true)
            else blockEntity
        }
        else -> null
    }
}

fun MachineBlockEntity<*>.transferFluids() {
    fluidComponent?.transferConfig?.forEach innerForEach@{ (direction, mode) ->
        if (mode == TransferMode.NONE) return@innerForEach
        var extractable: Storage<FluidVariant>? = null
        var insertable: Storage<FluidVariant>? = null
        if (mode.output) {
            insertable = fluidStorageOf(world as ServerWorld, pos, direction)
            extractable = fluidComponent
        }
        if (mode.input) {
            extractable = fluidStorageOf(world as ServerWorld, pos, direction)
            insertable = fluidComponent

        }
        if (extractable != null && insertable != null)
            StorageUtil.move(extractable, insertable, { true }, getFluidTransferRate(), null)
    }
}

fun MachineBlockEntity<*>.transferEnergy() {
    val world = world as ServerWorld
    Direction.values()
        .forEach { direction ->
            if (validConnections.contains(direction)) {
                val sourceIo = energyOf(world, pos, direction)
                val targetIo = energyOf(world, pos.offset(direction.opposite), direction.opposite)
                if (sourceIo == null || targetIo == null)
                    validConnections.remove(direction)
                else if (sourceIo.supportsExtraction() && targetIo.supportsInsertion())
                    EnergyMovement.move(sourceIo, targetIo, Double.MAX_VALUE)
            }
        }
}
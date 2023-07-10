package me.steven.indrev.utils

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import team.reborn.energy.api.EnergyStorage
import java.util.function.LongFunction

fun energyOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): EnergyStorage? {
    return world.energyIoCache.computeIfAbsent(
        blockPos.asLong(),
        Long2ObjectFunction { BlockApiCache.create(EnergyStorage.SIDED, world, blockPos) }).find(direction)
}

fun energyOf(itemStack: ItemStack?): EnergyStorage? {
    return if (itemStack == null || itemStack.isEmpty) null
    else EnergyStorage.ITEM.find(itemStack, ContainerItemContext.withConstant(itemStack))
}

fun energyOf(inv: Inventory?, slot: Int): EnergyStorage? {
    val itemStack = inv?.getStack(slot)
    return if (itemStack == null || itemStack.isEmpty) null
    else EnergyStorage.ITEM.find(itemStack, ContainerItemContext.ofSingleSlot(InventoryStorage.of(inv, null).getSlot(slot)))
}

fun extract(inv: Inventory?, slot: Int, amount: Long): Boolean {
    return energyOf(inv, slot)?.use(amount) == true
}

fun insert(inv: Inventory?, slot: Int, amount: Long): Boolean {
    val e = energyOf(inv, slot) ?: return false
    Transaction.openOuter().use { t ->
        val inserted = e.insert(amount, t)
        if (inserted == amount) {
            t.commit()
            return true
        }
        return false
    }
}

fun EnergyStorage.use(amount: Long): Boolean {
    Transaction.openOuter().use { t ->
        val extracted = this.extract(amount, t)
        if (extracted == amount) {
            t.commit()
            return true
        }
        return false
    }
}

fun EnergyStorage.insert(amount: Long, act: Boolean): Long {
    Transaction.openOuter().use { t ->
        val inserted = this.insert(amount, t)
        if (act) {
            t.commit()
        }
        return inserted
    }
}

fun EnergyStorage.extract(amount: Long, act: Boolean): Long {
    Transaction.openOuter().use { t ->
        val extracted = this.extract(amount, t)
        if (extracted == amount && act) {
            t.commit()
        }
        return extracted
    }
}

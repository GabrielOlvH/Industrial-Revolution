package me.steven.indrev.utils

import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.function.LongFunction

fun energyOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): EnergyIo? {
    return world.energyIoCache.computeIfAbsent(
        blockPos.asLong(),
        LongFunction { BlockApiCache.create(EnergyApi.SIDED, world, blockPos) }).find(direction)
}

fun energyOf(itemStack: ItemStack?): EnergyIo? {
    return if (itemStack == null) null
    else EnergyApi.ITEM.find(itemStack, null)
}

fun extract(itemStack: ItemStack?, amount: Double): Boolean {
    return energyOf(itemStack)?.use(amount) == true
}

fun EnergyIo.use(amount: Double): Boolean {
    if (extract(amount, Simulation.SIMULATE) == amount) {
        extract(amount, Simulation.ACT)
        return true
    }
    return false
}

package me.steven.indrev.utils

import dev.technici4n.fasttransferlib.api.ContainerItemContext
import dev.technici4n.fasttransferlib.api.Simulation
import dev.technici4n.fasttransferlib.api.energy.EnergyApi
import dev.technici4n.fasttransferlib.api.energy.EnergyIo
import dev.technici4n.fasttransferlib.api.item.ItemKey
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*
import java.util.function.LongFunction

private val ENERGY_IO_CACHE = WeakHashMap<World, Long2ObjectOpenHashMap<BlockApiCache<EnergyIo, Direction>>>()

fun energyOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): EnergyIo? {
    return ENERGY_IO_CACHE.computeIfAbsent(world) { Long2ObjectOpenHashMap() }.computeIfAbsent(
        blockPos.asLong(),
        LongFunction { BlockApiCache.create(EnergyApi.SIDED, world, blockPos) })[direction]
}

fun energyOf(itemStack: ItemStack?): EnergyIo? {
    return if (itemStack == null) null
    else EnergyApi.ITEM.get(ItemKey.of(itemStack), ContainerItemContext.ofStack(itemStack))
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

operator fun BlockApiLookup<EnergyIo, Direction>.get(world: World, pos: BlockPos, direction: Direction) = this.find(world, pos, direction)

operator fun BlockApiCache<EnergyIo, Direction>.get(direction: Direction) = this.find(direction)

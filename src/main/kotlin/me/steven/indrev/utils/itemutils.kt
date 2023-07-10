package me.steven.indrev.utils

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*
import java.util.function.LongFunction

typealias ItemFilter = (ItemVariant) -> Boolean

val itemApiCache = WeakHashMap<World, Long2ObjectOpenHashMap<BlockApiCache<Storage<ItemVariant>, Direction?>>>()

fun itemStorageOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): Storage<ItemVariant>? {
    return itemApiCache
        .computeIfAbsent(world) { Long2ObjectOpenHashMap() }
        .computeIfAbsent(blockPos.asLong(), Long2ObjectFunction { BlockApiCache.create(ItemStorage.SIDED, world, blockPos) })
        .find(direction)
}

fun itemStorageOf(world: World, blockPos: BlockPos, direction: Direction): Storage<ItemVariant>? {
    return if (world is ServerWorld) itemStorageOf(world, blockPos, direction) else ItemStorage.SIDED.find(world, blockPos, direction)
}
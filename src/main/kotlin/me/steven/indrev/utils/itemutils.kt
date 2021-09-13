package me.steven.indrev.utils

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.item.ItemAttributes
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import java.util.*
import java.util.function.LongFunction

fun itemInsertableOf(world: World, pos: BlockPos, direction: Direction) = ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))

fun itemExtractableOf(world: World, pos: BlockPos, direction: Direction) = ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))

fun groupedItemInv(world: World, pos: BlockPos, direction: Direction) = ItemAttributes.GROUPED_INV.get(world, pos, SearchOptions.inDirection(direction))

typealias ItemFilter = (ItemVariant) -> Boolean

val itemApiCache = WeakHashMap<World, Long2ObjectOpenHashMap<BlockApiCache<Storage<ItemVariant>, Direction>>>()

fun itemStorageOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): Storage<ItemVariant>? {
    return itemApiCache
        .computeIfAbsent(world) { Long2ObjectOpenHashMap() }
        .computeIfAbsent(blockPos.asLong(), LongFunction { BlockApiCache.create(ItemStorage.SIDED, world, blockPos) })
        .find(direction)
}

fun itemStorageOf(world: World, blockPos: BlockPos, direction: Direction): Storage<ItemVariant>? {
    return if (world is ServerWorld) itemStorageOf(world, blockPos, direction) else ItemStorage.SIDED.find(world, blockPos, direction)
}
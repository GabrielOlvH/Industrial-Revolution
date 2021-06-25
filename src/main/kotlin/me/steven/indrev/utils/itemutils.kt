package me.steven.indrev.utils

import alexiil.mc.lib.attributes.SearchOptions
import alexiil.mc.lib.attributes.item.ItemAttributes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

fun itemInsertableOf(world: World, pos: BlockPos, direction: Direction) = ItemAttributes.INSERTABLE.get(world, pos, SearchOptions.inDirection(direction))

fun itemExtractableOf(world: World, pos: BlockPos, direction: Direction) = ItemAttributes.EXTRACTABLE.get(world, pos, SearchOptions.inDirection(direction))

fun groupedItemInv(world: World, pos: BlockPos, direction: Direction) = ItemAttributes.GROUPED_INV.get(world, pos, SearchOptions.inDirection(direction))
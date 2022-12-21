package me.steven.indrev.utils

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.client.gui.screen.Screen
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.WeakHashMap
import java.util.function.LongFunction

const val bucket = 81000L

val fluidApiCache = WeakHashMap<World, Long2ObjectOpenHashMap<BlockApiCache<Storage<FluidVariant>, Direction>>>()

fun fluidStorageOf(world: ServerWorld, blockPos: BlockPos, direction: Direction): Storage<FluidVariant>? {
    return fluidApiCache
        .computeIfAbsent(world) { Long2ObjectOpenHashMap() }
        .computeIfAbsent(blockPos.asLong(), LongFunction { BlockApiCache.create(FluidStorage.SIDED, world, blockPos) })
        .find(direction)
}


fun fluidStorageOf(world: World, blockPos: BlockPos, direction: Direction): Storage<FluidVariant>? {
    if (world is ServerWorld)
        return fluidStorageOf(world, blockPos, direction)
    else return FluidStorage.SIDED.find(world, blockPos, direction)
}

fun fluidStorageOf(itemStack: ItemStack?): Storage<FluidVariant>? {
    return if (itemStack == null) null
    else FluidStorage.ITEM.find(itemStack, null)
}

fun getFluidTooltip(variant: FluidVariant, amount: Long, capacity: Long): List<Text> {
    val tooltips = mutableListOf<Text>()
    val id = Registry.BLOCK.getId(variant.fluid.defaultState.blockState.block)
    val color = FluidRenderHandlerRegistry.INSTANCE.get(variant.fluid)?.getFluidColor(null, null, variant.fluid.defaultState) ?: -1

    tooltips.add(Text.translatable("block.${id.namespace}.${id.path}").styled { s -> s.withColor(color) })

    val asMb = amount / 81
    val accurate = amount / 81.0
    val prefix = when {
        accurate > asMb -> ">"
        accurate < asMb -> "<"
        else -> ""
    }
    if (capacity > 0)
        tooltips.add(Text.translatable("$prefix$asMb / ${capacity / 81} mB"))
    else
        tooltips.add(Text.translatable("$prefix$asMb mB"))

    if (Screen.hasShiftDown()) {
        if (capacity > 0)
            tooltips.add(Text.translatable("$amount / $capacity droplets"))
        else
            tooltips.add(Text.translatable("$amount droplets"))
    }

    return tooltips
}


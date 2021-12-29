package me.steven.indrev.utils

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.client.gui.screen.Screen
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.TranslatableText
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.*
import java.util.function.LongFunction


//these are just for reference, they're not used much

val bucket = 81000L

val bottle = bucket / 3 // 27000 droplets

val half_bucket = bucket / 2 // 40500 droplets

val block = bucket
val ingot = block / 9 // 9000 droplets
val nugget = ingot / 9 // 1000 droplets
val scrap = ingot / 4 // 250 droplets

val mb = bucket / 1000

fun FluidBlock.drainFluid(world: World, pos: BlockPos, state: BlockState): Fluid {
    return if (state.get(FluidBlock.LEVEL) as Int == 0) {
        world.setBlockState(pos, Blocks.AIR.defaultState, 11)
        fluid
    } else {
        Fluids.EMPTY
    }
}

//TODO fuck weakhashmaps
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

typealias IRFluidAmount = ResourceAmount<FluidVariant>

fun IRFluidAmount.toPacket(buf: PacketByteBuf) {
    resource.toPacket(buf)
    buf.writeLong(amount)
}

fun IRFluidAmount.renderGuiRect(x0: Double, y0: Double, x1: Double, y1: Double) {
    FluidKeys.get(resource.fluid).withAmount(FluidAmount.BUCKET).renderGuiRect(x0, y0, x1, y1)
}

fun fromPacket(buf: PacketByteBuf): IRFluidAmount {
    val res = FluidVariant.fromPacket(buf)
    val amt = buf.readLong()
    return amt of res
}

infix fun Long.of(variant: FluidVariant) = IRFluidAmount(variant, this)

fun getTooltip(variant: FluidVariant, amount: Long, capacity: Long): List<OrderedText> {
    val tooltips = mutableListOf<OrderedText>()
    val id = Registry.BLOCK.getId(variant.fluid.defaultState.blockState.block)
    val color = FluidRenderHandlerRegistry.INSTANCE.get(variant.fluid)?.getFluidColor(null, null, variant.fluid.defaultState) ?: -1

    tooltips.add(TranslatableText("block.${id.namespace}.${id.path}").setStyle(Style.EMPTY.withColor(color)).asOrderedText())

    val asMb = amount / 81
    val accurate = amount / 81.0
    val prefix = when {
        accurate > asMb -> ">"
        accurate < asMb -> "<"
        else -> ""
    }
    if (capacity > 0)
        tooltips.add(TranslatableText("$prefix$asMb / ${capacity / 81} mB").asOrderedText())
    else
        tooltips.add(TranslatableText("$prefix$asMb mB").asOrderedText())

    if (Screen.hasShiftDown()) {
        if (capacity > 0)
            tooltips.add(TranslatableText("$amount / $capacity droplets").asOrderedText())
        else
            tooltips.add(TranslatableText("$amount droplets").asOrderedText())
    }
    
    return tooltips

}

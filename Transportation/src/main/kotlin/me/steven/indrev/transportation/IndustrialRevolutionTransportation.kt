package me.steven.indrev.transportation

import me.steven.indrev.api.Tier
import me.steven.indrev.transportation.blocks.CableBlock
import me.steven.indrev.transportation.blocks.FluidPipeBlock
import me.steven.indrev.transportation.blocks.ItemPipeBlock
import me.steven.indrev.transportation.blocks.PipeBlockEntity
import me.steven.indrev.transportation.events.ServerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

const val MOD_ID = "indrev-transportation"

val ITEM_PIPE_MK1_BLOCK = ItemPipeBlock(Tier.MK1)
val ITEM_PIPE_MK1_BLOCK_ITEM = BlockItem(ITEM_PIPE_MK1_BLOCK, Item.Settings())
val ITEM_PIPE_MK2_BLOCK = ItemPipeBlock(Tier.MK2)
val ITEM_PIPE_MK2_BLOCK_ITEM = BlockItem(ITEM_PIPE_MK2_BLOCK, Item.Settings())
val ITEM_PIPE_MK3_BLOCK = ItemPipeBlock(Tier.MK3)
val ITEM_PIPE_MK3_BLOCK_ITEM = BlockItem(ITEM_PIPE_MK3_BLOCK, Item.Settings())
val ITEM_PIPE_MK4_BLOCK = ItemPipeBlock(Tier.MK4)
val ITEM_PIPE_MK4_BLOCK_ITEM = BlockItem(ITEM_PIPE_MK4_BLOCK, Item.Settings())

val CABLE_MK1_BLOCK = CableBlock(Tier.MK1)
val CABLE_MK1_BLOCK_ITEM = BlockItem(CABLE_MK1_BLOCK, Item.Settings())
val CABLE_MK2_BLOCK = CableBlock(Tier.MK2)
val CABLE_MK2_BLOCK_ITEM = BlockItem(CABLE_MK2_BLOCK, Item.Settings())
val CABLE_MK3_BLOCK = CableBlock(Tier.MK3)
val CABLE_MK3_BLOCK_ITEM = BlockItem(CABLE_MK3_BLOCK, Item.Settings())
val CABLE_MK4_BLOCK = CableBlock(Tier.MK4)
val CABLE_MK4_BLOCK_ITEM = BlockItem(CABLE_MK4_BLOCK, Item.Settings())

val FLUID_PIPE_MK1_BLOCK = FluidPipeBlock(Tier.MK1)
val FLUID_PIPE_MK1_BLOCK_ITEM = BlockItem(FLUID_PIPE_MK1_BLOCK, Item.Settings())
val FLUID_PIPE_MK2_BLOCK = FluidPipeBlock(Tier.MK2)
val FLUID_PIPE_MK2_BLOCK_ITEM = BlockItem(FLUID_PIPE_MK2_BLOCK, Item.Settings())
val FLUID_PIPE_MK3_BLOCK = FluidPipeBlock(Tier.MK3)
val FLUID_PIPE_MK3_BLOCK_ITEM = BlockItem(FLUID_PIPE_MK3_BLOCK, Item.Settings())
val FLUID_PIPE_MK4_BLOCK = FluidPipeBlock(Tier.MK4)
val FLUID_PIPE_MK4_BLOCK_ITEM = BlockItem(FLUID_PIPE_MK4_BLOCK, Item.Settings())


val PIPE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(::PipeBlockEntity, ITEM_PIPE_MK1_BLOCK, ITEM_PIPE_MK2_BLOCK, ITEM_PIPE_MK3_BLOCK, ITEM_PIPE_MK4_BLOCK, CABLE_MK1_BLOCK, CABLE_MK2_BLOCK, CABLE_MK3_BLOCK, CABLE_MK4_BLOCK, FLUID_PIPE_MK1_BLOCK, FLUID_PIPE_MK2_BLOCK, FLUID_PIPE_MK3_BLOCK, FLUID_PIPE_MK4_BLOCK).build()

fun init() {
    Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(MOD_ID, "pipe"), PIPE_BLOCK_ENTITY)

    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "item_pipe_mk1"), ITEM_PIPE_MK1_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "item_pipe_mk1"), ITEM_PIPE_MK1_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "item_pipe_mk2"), ITEM_PIPE_MK2_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "item_pipe_mk2"), ITEM_PIPE_MK2_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "item_pipe_mk3"), ITEM_PIPE_MK3_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "item_pipe_mk3"), ITEM_PIPE_MK3_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "item_pipe_mk4"), ITEM_PIPE_MK4_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "item_pipe_mk4"), ITEM_PIPE_MK4_BLOCK_ITEM)

    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "cable_mk1"), CABLE_MK1_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "cable_mk1"), CABLE_MK1_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "cable_mk2"), CABLE_MK2_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "cable_mk2"), CABLE_MK2_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "cable_mk3"), CABLE_MK3_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "cable_mk3"), CABLE_MK3_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "cable_mk4"), CABLE_MK4_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "cable_mk4"), CABLE_MK4_BLOCK_ITEM)

    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fluid_pipe_mk1"), FLUID_PIPE_MK1_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "fluid_pipe_mk1"), FLUID_PIPE_MK1_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fluid_pipe_mk2"), FLUID_PIPE_MK2_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "fluid_pipe_mk2"), FLUID_PIPE_MK2_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fluid_pipe_mk3"), FLUID_PIPE_MK3_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "fluid_pipe_mk3"), FLUID_PIPE_MK3_BLOCK_ITEM)
    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "fluid_pipe_mk4"), FLUID_PIPE_MK4_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "fluid_pipe_mk4"), FLUID_PIPE_MK4_BLOCK_ITEM)

    ServerLifecycleEvents.SERVER_STOPPED.register(ServerEvents)
    ServerTickEvents.START_WORLD_TICK.register(ServerEvents)
}
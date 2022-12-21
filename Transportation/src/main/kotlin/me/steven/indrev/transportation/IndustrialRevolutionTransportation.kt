package me.steven.indrev.transportation

import me.steven.indrev.transportation.blocks.CableBlock
import me.steven.indrev.transportation.blocks.ItemPipeBlock
import me.steven.indrev.transportation.blocks.PipeBlock
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

val ITEM_PIPE_BLOCK = ItemPipeBlock()
val ITEM_PIPE_BLOCK_ITEM = BlockItem(ITEM_PIPE_BLOCK, Item.Settings())

val CABLE_BLOCK = CableBlock()
val CABLE_BLOCK_ITEM = BlockItem(CABLE_BLOCK, Item.Settings())

val PIPE_BLOCK_ENTITY = FabricBlockEntityTypeBuilder.create(::PipeBlockEntity, ITEM_PIPE_BLOCK, CABLE_BLOCK).build()

fun init() {
    Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(MOD_ID, "pipe"), PIPE_BLOCK_ENTITY)

    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "item_pipe"), ITEM_PIPE_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "item_pipe"), ITEM_PIPE_BLOCK_ITEM)

    Registry.register(Registry.BLOCK, Identifier(MOD_ID, "cable"), CABLE_BLOCK)
    Registry.register(Registry.ITEM, Identifier(MOD_ID, "cable"), CABLE_BLOCK_ITEM)

    ServerLifecycleEvents.SERVER_STOPPED.register(ServerEvents)
    ServerTickEvents.START_WORLD_TICK.register(ServerEvents)
}
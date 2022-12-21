package me.steven.indrev

import me.steven.indrev.blocks.PULVERIZER
import me.steven.indrev.blocks.registerBlocks
import me.steven.indrev.items.registerItems
import me.steven.indrev.events.common.DatapackReloadEvent
import me.steven.indrev.events.common.WorldEndTickEvent
import me.steven.indrev.packets.common.CommonPackets
import me.steven.indrev.recipes.PULVERIZER_RECIPE_TYPE
import me.steven.indrev.screens.registerScreenHandlers
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

internal const val MOD_ID = "indrev-base"

internal val GROUP = FabricItemGroupBuilder.create(identifier("base")).build()

fun init() {
    registerBlocks()
    registerItems()
    registerScreenHandlers()

    PULVERIZER_RECIPE_TYPE
    PULVERIZER

    CommonPackets.register()

    ServerTickEvents.END_WORLD_TICK.register(WorldEndTickEvent)
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(DatapackReloadEvent)
}
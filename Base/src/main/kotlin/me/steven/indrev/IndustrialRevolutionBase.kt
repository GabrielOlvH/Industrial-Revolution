package me.steven.indrev

import me.steven.indrev.blocks.PULVERIZER
import me.steven.indrev.blocks.registerBlocks
import me.steven.indrev.items.registerItems
import me.steven.indrev.events.common.DatapackReloadEvent
import me.steven.indrev.events.common.WorldEndTickEvent
import me.steven.indrev.items.ALL_ITEMS
import me.steven.indrev.packets.common.CommonPackets
import me.steven.indrev.recipes.PULVERIZER_RECIPE_TYPE
import me.steven.indrev.screens.registerScreenHandlers
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.item.ItemGroup
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text

internal const val MOD_ID = "indrev-base"

internal val GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, identifier("base"))

fun init() {

    registerBlocks()
    registerItems()
    registerScreenHandlers()

    PULVERIZER_RECIPE_TYPE
    PULVERIZER

    CommonPackets.register()

    ServerTickEvents.END_WORLD_TICK.register(WorldEndTickEvent)
    ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(DatapackReloadEvent)

    Registry.register(Registries.ITEM_GROUP, GROUP,
        FabricItemGroup.builder()
            .displayName(Text.literal("indrev-base.group"))
            .entries { _, entries -> ALL_ITEMS.forEach(entries::add) }
            .build())
}
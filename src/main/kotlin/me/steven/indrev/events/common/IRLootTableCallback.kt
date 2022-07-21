package me.steven.indrev.events.common

import me.steven.indrev.registry.IRItemRegistry
import net.fabricmc.fabric.api.loot.v2.LootTableEvents
import net.fabricmc.fabric.api.loot.v2.LootTableSource
import net.minecraft.loot.LootManager
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object IRLootTableCallback : LootTableEvents.Modify {

    override fun modifyLootTable(
        resourceManager: ResourceManager,
        lootManager: LootManager?,
        id: Identifier?,
        tableBuilder: LootTable.Builder,
        source: LootTableSource?
    ) {
        val chance = when (id) {
            abandonedMineshaft, simpleDungeon -> 0.3f
            buriedTreasure -> 0.4f
            woodlandMansion -> 0.5f
            endCityTreasure -> 0.6f
            else -> return
        }
        val builder = LootPool.builder()
        builder.rolls(ConstantLootNumberProvider.create(1f))
        colorModules.forEach { builder.with(ItemEntry.builder(it)) }
        builder.conditionally(RandomChanceLootCondition.builder(chance))
        tableBuilder.pool(builder.build())
    }

    private val abandonedMineshaft = Identifier("chests/abandoned_mineshaft")
    private val buriedTreasure = Identifier("chests/buried_treasure")
    private val simpleDungeon = Identifier("chests/simple_dungeon")
    private val woodlandMansion = Identifier("chests/woodland_mansion")
    private val endCityTreasure = Identifier("chests/end_city_treasure")
    private val colorModules = arrayOf(
        IRItemRegistry.PINK_MODULE_ITEM,
        IRItemRegistry.RED_MODULE_ITEM,
        IRItemRegistry.PURPLE_MODULE_ITEM,
        IRItemRegistry.BLUE_MODULE_ITEM,
        IRItemRegistry.CYAN_MODULE_ITEM,
        IRItemRegistry.GREEN_MODULE_ITEM,
        IRItemRegistry.YELLOW_MODULE_ITEM,
        IRItemRegistry.ORANGE_MODULE_ITEM,
        IRItemRegistry.BLUE_MODULE_ITEM,
        IRItemRegistry.BLACK_MODULE_ITEM,
        IRItemRegistry.BROWN_MODULE_ITEM
    )
}
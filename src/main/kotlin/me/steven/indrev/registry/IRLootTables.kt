package me.steven.indrev.registry

import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback
import net.minecraft.loot.ConstantLootTableRange
import net.minecraft.loot.LootPool
import net.minecraft.loot.condition.RandomChanceLootCondition
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.util.Identifier

object IRLootTables {
    fun register() {
        LootTableLoadingCallback.EVENT.register(
            LootTableLoadingCallback { _, _, id, supplier, _ ->
                val chance = when (id) {
                    abandonedMineshaft, simpleDungeon -> 0.3f
                    buriedTreasure -> 0.4f
                    woodlandMansion -> 0.5f
                    endCityTreasure -> 0.6f
                    else -> return@LootTableLoadingCallback
                }
                val builder = LootPool.builder()
                builder.rolls(ConstantLootTableRange.create(1))
                colorModules.forEach { builder.with(ItemEntry.builder(it)) }
                builder.conditionally(RandomChanceLootCondition.builder(chance))
                supplier.withPool(builder.build())
            }
        )
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
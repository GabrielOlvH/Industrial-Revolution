package me.steven.indrev.datagen.generators

import com.google.gson.JsonObject
import me.steven.indrev.datagen.DataGenerator
import me.steven.indrev.datagen.JsonFactory
import net.minecraft.block.Block
import net.minecraft.item.Items
import net.minecraft.loot.LootManager
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.UniformLootTableRange
import net.minecraft.loot.condition.SurvivesExplosionLootCondition
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.util.registry.Registry
import java.io.File

class LootTableGenerator(val root: File, namespace: String, fallback: (Block) -> JsonFactory<Block>)
    : DataGenerator<Block, JsonObject?>(File(root, "loot_tables/blocks"), namespace, fallback) {

    override fun generate(): Int {
        var count = 0
        Registry.BLOCK.ids.filter { id -> id.namespace == namespace }.forEach {
            val block = Registry.BLOCK.get(it)
            if (block.asItem() != null && block.asItem() != Items.AIR && generate(it, block)) {
                count++
            }
        }
        return count
    }

    companion object {
        val SELF_DROP: (Block) -> JsonFactory<Block> = { block ->
            object : JsonFactory<Block> {
                override fun generate(): JsonObject? {
                    val lootTable = LootTable.builder()
                        .pool(
                            LootPool.builder().rolls(UniformLootTableRange(1f)).with(ItemEntry.builder(block))
                                .conditionally(SurvivesExplosionLootCondition.builder())
                        )
                        .type(LootContextTypes.BLOCK)
                        .build()
                    return LootManager.toJson(lootTable).asJsonObject
                }

            }
        }
    }
}
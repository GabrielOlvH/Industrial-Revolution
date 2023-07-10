package me.steven.indrev.datagen.generators

import com.google.gson.JsonObject
import me.steven.indrev.datagen.DataGenerator
import me.steven.indrev.datagen.JsonFactory
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.Items
import net.minecraft.loot.LootManager
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.condition.MatchToolLootCondition
import net.minecraft.loot.condition.SurvivesExplosionLootCondition
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.entry.AlternativeEntry
import net.minecraft.loot.entry.ItemEntry
import net.minecraft.loot.function.ApplyBonusLootFunction
import net.minecraft.loot.function.ExplosionDecayLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.predicate.NumberRange
import net.minecraft.predicate.item.EnchantmentPredicate
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import net.minecraft.registry.Registry
import java.io.File

class LootTableGenerator(val root: File, namespace: String, fallback: (Block) -> JsonFactory<Block>)
    : DataGenerator<Block, JsonObject?>(File(root, "loot_tables/blocks"), namespace, fallback) {

    override fun generate(): Int {
        var count = 0
        Registries.BLOCK.ids.filter { id -> id.namespace == namespace }.forEach {
            val block = Registries.BLOCK.get(it)
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
                            LootPool.builder().rolls(ConstantLootNumberProvider.create(1f)).with(ItemEntry.builder(block))
                                .conditionally(SurvivesExplosionLootCondition.builder())
                        )
                        .type(LootContextTypes.BLOCK)
                        .build()
                    return null //LootManager.toJson(lootTable).asJsonObject
                }

            }
        }

        val ORE_DROP: (Block) -> JsonFactory<Block> = { block ->
            object : JsonFactory<Block> {
                override fun generate(): JsonObject? {
                    val blockId = Registries.BLOCK.getId(block)
                    val rawOreId = "raw_${blockId.path.replace("deepslate_", "").replace("_ore", "")}"
                    val rawOre = Registries.ITEM.get(Identifier(blockId.namespace, rawOreId))
                    val lootTable = LootTable.builder()
                        .pool(
                            LootPool.builder()
                                .bonusRolls(ConstantLootNumberProvider.create(0f))
                                .rolls(ConstantLootNumberProvider.create(1f))
                                .with(
                                    AlternativeEntry.builder(
                                        ItemEntry.builder(block)
                                            .conditionally(
                                                MatchToolLootCondition.builder(
                                                    ItemPredicate.Builder.create().enchantment(
                                                        EnchantmentPredicate(
                                                            Enchantments.SILK_TOUCH,
                                                            NumberRange.IntRange.atLeast(1)
                                                        )
                                                    )
                                                )
                                            ),
                                        ItemEntry.builder(rawOre)
                                            .apply(ApplyBonusLootFunction.oreDrops(Enchantments.FORTUNE))
                                            .apply(ExplosionDecayLootFunction.builder())
                                    )
                                )
                        )


                        .type(LootContextTypes.BLOCK)
                        .build()
                    return null//LootManager.toJson(lootTable).asJsonObject
                }
            }
        }
    }
}
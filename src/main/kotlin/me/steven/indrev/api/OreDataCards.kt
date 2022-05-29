package me.steven.indrev.api

import me.steven.indrev.config.IRConfig
import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random
import net.minecraft.util.registry.Registry
import kotlin.math.pow

object OreDataCards {

    const val MAX_SIZE = 2048
    const val MAX_RICNHESS = 1.0
    const val MAX_PER_CYCLE = 8

    val INVALID_DATA = Data(emptyList(), mutableMapOf(), -1.0, -1, -1, -1, -1, -1)

    fun isAllowed(stack: ItemStack): Boolean {
        return IRConfig.miningRigConfig.allowedTags.any { stack.isIn(TagKey.of(Registry.ITEM_KEY, Identifier(it.key))) }
    }

    fun getCost(stack: ItemStack): Int {
        return IRConfig.miningRigConfig.allowedTags.firstNotNullOfOrNull { if (stack.isIn(TagKey.of(Registry.ITEM_KEY, Identifier(it.key)))) it.value else null } ?: 0
    }

    fun readNbt(stack: ItemStack): Data? {
        if (!stack.isOf(IRItemRegistry.ORE_DATA_CARD)) return null
        val nbt = stack.getSubNbt("CardData") ?: return null

        val entriesNbt = nbt.getList("Entries", 10)
        val entries = mutableListOf<OreEntry>()
        entriesNbt.forEach { element ->
            val itemId = (element as NbtCompound).getString("ItemId")
            val optional = Registry.ITEM.getOrEmpty(Identifier(itemId))
            if (!optional.isPresent) {
                return INVALID_DATA
            }

            val count = element.getInt("Count")
            entries.add(OreEntry(optional.get(), count))
        }

        val modifiersNbt = nbt.getList("ModifiersUsed", 10)
        val modifiers = mutableMapOf<Modifier, Int>()
        modifiersNbt.forEach { element ->
            val modifierId = (element as NbtCompound).getInt("Modifier")
            val level = element.getInt("Level")
            modifiers[Modifier.values()[modifierId]] = level
        }

        val richness = nbt.getDouble("Richness")
        val size = nbt.getInt("Size")
        val used = nbt.getInt("Used")
        val speed = nbt.getInt("Speed")
        val rng = nbt.getInt("Rng")
        val energy = nbt.getInt("Energy")

        return Data(entries.toList(), modifiers, richness, speed, rng, energy, size, used)
    }

    fun writeNbt(stack: ItemStack, data: Data) {
        if (!stack.isOf(IRItemRegistry.ORE_DATA_CARD)) return
        val nbt = stack.getOrCreateSubNbt("CardData")

        val entriesNbt = NbtList()
        data.entries.forEach { entry ->
            val entryNbt = NbtCompound()
            entryNbt.putString("ItemId", Registry.ITEM.getId(entry.item).toString())
            entryNbt.putInt("Count", entry.count)
            entriesNbt.add(entryNbt)
        }
        nbt.put("Entries", entriesNbt)

        val modifiersNbt = NbtList()
        data.modifiersUsed.forEach { (modifier, level) ->
            val modifierNbt = NbtCompound()
            modifierNbt.putInt("Modifier", modifier.ordinal)
            modifierNbt.putInt("Level", level)
            modifiersNbt.add(modifierNbt)
        }
        nbt.put("ModifiersUsed", modifiersNbt)

        nbt.putDouble("Richness", data.richness)
        nbt.putInt("Size", data.maxCycles)
        nbt.putInt("Used", data.used)
        nbt.putInt("Speed", data.speed)
        nbt.putInt("Rng", data.rng)
        nbt.putInt("Energy", data.energyRequired)
    }

    data class Data(val entries: List<OreEntry>, val modifiersUsed: MutableMap<Modifier, Int>, val richness: Double, val speed: Int, val rng: Int, val energyRequired: Int, val maxCycles: Int, var used: Int) {
        fun isValid(): Boolean {
            return this != INVALID_DATA && entries.isNotEmpty() && richness > 0 && maxCycles > 0 && maxCycles < MAX_SIZE
        }

        fun isEmpty(): Boolean {
            return used >= maxCycles
        }

        fun pickRandom(random: Random): Item {
            entries.forEach { entry ->
                entry.order = -random.nextFloat().pow(1.0f / entry.count.toFloat())
            }
            return entries.minByOrNull { a -> a.order }!!.item
        }
    }

    data class OreEntry(val item: Item, val count: Int, var order: Float = 0.0f)

    enum class Modifier(val item: Item) {
        RICHNESS(IRItemRegistry.ENRICHED_NIKOLITE_DUST()),
        SPEED(Items.REDSTONE),
        SIZE(Items.STONE),
        RNG(Items.EMERALD);

        val translationKey = "item.indrev.ore_data_card.modifier.${name.lowercase()}"

        companion object {
            fun isModifierItem(item: Item): Boolean = values().any { it.item == item }

            fun byItem(item: Item): Modifier? = values().firstOrNull { it.item == item }
        }
    }
}
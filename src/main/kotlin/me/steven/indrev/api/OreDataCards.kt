package me.steven.indrev.api

import me.steven.indrev.registry.IRItemRegistry
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.*
import kotlin.math.pow

object OreDataCards {

    const val MAX_SIZE = 8192
    const val MAX_RICNHESS = 1.0
    const val MAX_PER_CYCLE = 24

    val MINING_RIG_ALLOWED_TAG: TagKey<Item> = TagKey.of(Registry.ITEM_KEY, Identifier("indrev:mining_rig_allowed"))

    val INVALID_DATA = Data(emptyList(), mutableMapOf(), -1.0, -1, -1, -1, -1, -1)

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

    /**
     * CREATING:
     * Richness start at 1.0
     * Size starts at 64 and caps at 2^5 (32768)
     * Overflowing will corrupt the data card, making it unusable
     * Ores need to be inserted by 64 stack
     * Inserted Ore ->
     *      Size = Size + 256
     *      Richness decreases 0.025 for every stack
     * After inserting different ore type ->
     *      Size = Size + 1024
     *      Richness decreases by 0.05 for every stack now
     *
     *
     * Modifiers:
     *      Enriched Nikolite Dust: increases richness by 0.01 per stack of 16 (max increase is 0.2)
     *      Redstone: decreases ticks per cycle by 1 tick per stack and increases energy required
     *      Stone: increases size by a 100 per stack but increases ticks per cycle by 2 ticks
     *      Emerald: luck mechanic
     *
     *
     * "Luck" mechanic:
     *      After inserting an emerald, you have: (affected by luck effect)
     *      95% chance of nothing happening
     *      3% chance of getting "bad RNG"
     *      2% chance of getting "good RNG"
     *      Bad RNG will have a 10% chance of turning a cycle product into stone/trash
     *      Good RNG will have a 10% chance of doubling a cycle product
     *      After getting bad or good rng you can no longer use emeralds
     *
     *
     *
     * USING:
     * Each cycle increases 'used' by 1
     * Cycle ticks required = 5 + richness * 35 + modifiers
     * Ore chance = percentage
     * Stack size = (10 * richness) + random(MAX_PER_CYCLE * richness) + modifiers
     */
    data class Data(val entries: List<OreEntry>, val modifiersUsed: MutableMap<Modifier, Int>, val richness: Double, val speed: Int, val rng: Int, val energyRequired: Int, val maxCycles: Int, var used: Int) {
        fun isValid(): Boolean {
            return this != INVALID_DATA && entries.isNotEmpty() && richness > 0 && maxCycles > 0 && maxCycles < MAX_SIZE
        }

        fun isEmpty(): Boolean {
            return used >= maxCycles
        }

        fun pickRandom(random: Random): Item {
            entries.forEach { entry ->
                entry.order = random.nextDouble().pow(1.0f / entry.count.toDouble())
            }
            return entries.minByOrNull { a -> a.order }!!.item
        }
    }

    data class OreEntry(val item: Item, val count: Int, var order: Double = 0.0)

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
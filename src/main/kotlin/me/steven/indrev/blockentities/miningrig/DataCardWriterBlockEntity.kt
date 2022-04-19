package me.steven.indrev.blockentities.miningrig

import me.steven.indrev.api.OreDataCards
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.autosync
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.registry.IRItemRegistry
import me.steven.indrev.registry.MachineRegistry
import net.minecraft.block.BlockState
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

class DataCardWriterBlockEntity (tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.DATA_CARD_WRITER_REGISTRY, pos, state) {
    init {
        this.inventoryComponent = inventory(this) {
            input {
                0 filter { itemStack, _ -> itemStack.item == IRItemRegistry.ORE_DATA_CARD }
                1 until 13 filter { stack, _ -> OreDataCards.isAllowed(stack) }
                13 until 16 filter { stack, _ -> OreDataCards.Modifier.isModifierItem(stack.item) }
            }
        }
    }

    var processTime by autosync(PROCESS_ID, 0)
    var totalProcessTime by autosync(TOTAL_PROCESS_ID, 0)

    private val modifiersToAdd = mutableMapOf<OreDataCards.Modifier, Int>()
    private val toWrite = mutableListOf<ItemStack>()

    override fun machineTick() {
        if (totalProcessTime > 0 && use(getEnergyCost())) {
            if (processTime >= totalProcessTime)
                finish()
            else {
                processTime++
                workingState = true
            }
        } else {
            workingState = false
        }
    }

    fun start() {
        val inventory = inventoryComponent!!.inventory
        val cardStack = inventory.getStack(0)
        val oldData = OreDataCards.readNbt(cardStack)

        ORES_SLOTS.forEach { slot ->
            val stack = inventory.getStack(slot)
            if (!stack.isEmpty && stack.count == 64) {
                toWrite.add(stack)
                inventory.setStack(slot, ItemStack.EMPTY)
            }
        }

        if (toWrite.isEmpty() && oldData == null) {
            return
        }

        MODIFIERS_SLOTS.forEach { slot ->
            val stack = inventory.getStack(slot)
            val modifier = OreDataCards.Modifier.byItem(stack.item)
            var level = (modifiersToAdd[modifier] ?: 0) + (oldData?.modifiersUsed?.get(modifier) ?: 0)
            when (modifier) {
                OreDataCards.Modifier.RICHNESS -> {
                    while (stack.count >= 16 && level < 40) {
                        stack.decrement(16)
                        level++
                    }
                }
                OreDataCards.Modifier.SPEED, OreDataCards.Modifier.SIZE -> {
                    while (stack.count >= 64) {
                        stack.decrement(64)
                        level++
                    }
                }
                OreDataCards.Modifier.RNG -> {
                    if (level == 0) {
                        stack.decrement(1)
                        val r = world!!.random.nextDouble()
                        if (r > 0.95 && r <= 0.98) {
                            level = -1
                        } else if (r > 0.98) {
                            level = 1
                        }
                    }
                }
                else -> return@forEach
            }
            modifiersToAdd[modifier] = level - (oldData?.modifiersUsed?.get(modifier) ?: 0)
        }

        processTime = 0
        totalProcessTime = 20*10 + (toWrite.size * (5*modifiersToAdd.map { it.value }.sum()))
    }

    private fun finish() {
        val inventory = inventoryComponent!!.inventory
        val cardStack = inventory.getStack(0)
        val oldData = OreDataCards.readNbt(cardStack)

        val oreTypes = toWrite.map { it.item }.distinct().count()
        val richnessDecrease = if (oreTypes == 1) 0.02 else 0.04
        val richnessModifier = ((modifiersToAdd[OreDataCards.Modifier.RICHNESS] ?: 0) * 0.01).coerceAtMost(0.2)
        val richness = ((oldData?.richness ?: 1.0) - (richnessDecrease * toWrite.size) + richnessModifier).coerceIn(richnessDecrease, 1.0)

        val speedModifier = ((oldData?.modifiersUsed?.get(OreDataCards.Modifier.SPEED) ?: 0) + (modifiersToAdd[OreDataCards.Modifier.SPEED] ?: 0)) * 20
        val speed = 100 + (richness * 1100) - speedModifier + (modifiersToAdd[OreDataCards.Modifier.SIZE] ?: 0) * 2

        val rng = oldData?.rng ?: modifiersToAdd[OreDataCards.Modifier.RNG] ?: 0

        val oreEnergyRequired = toWrite.sumOf { OreDataCards.getCost(it) * 16 }
        val energyRequired = (oldData?.energyRequired ?: 32) + 8 * (modifiersToAdd[OreDataCards.Modifier.SPEED] ?: 0) + oreEnergyRequired

        val cyclesModifiers = (modifiersToAdd[OreDataCards.Modifier.SIZE] ?: 0) * 128
        val maxCycles = (oldData?.maxCycles ?: 0) + (toWrite.size * 64) + cyclesModifiers

        val items = mutableMapOf<Item, Int>()
        oldData?.entries?.forEach { entry ->
            items[entry.item] = entry.count
        }
        toWrite.forEach { stack ->
            items[stack.item] = items.getOrDefault(stack.item, 0) + stack.count
        }
        val entries = items.keys.map { OreDataCards.OreEntry(it, items[it]!!) }

        val modifiersMap = mutableMapOf<OreDataCards.Modifier, Int>()
        modifiersToAdd.forEach { (modifier, level) ->
            modifiersMap[modifier] = (oldData?.modifiersUsed?.get(modifier)?: 0) + level
        }
        val data = OreDataCards.Data(entries, modifiersMap, richness, speed.toInt(), rng, energyRequired, maxCycles, oldData?.used ?: 0)

        OreDataCards.writeNbt(cardStack, data)

        modifiersToAdd.clear()
        toWrite.clear()
        processTime = 0
        totalProcessTime = 0
    }

    companion object {
        const val PROCESS_ID = 2
        const val TOTAL_PROCESS_ID = 3

        val MODIFIERS_SLOTS = 13 until 16
        val ORES_SLOTS = 1 until 13
    }
}
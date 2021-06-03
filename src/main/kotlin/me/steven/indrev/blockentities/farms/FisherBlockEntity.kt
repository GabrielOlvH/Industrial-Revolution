package me.steven.indrev.blockentities.farms

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.toVec3d
import net.minecraft.block.BlockState
import net.minecraft.item.FishingRodItem
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class FisherBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.FISHER_REGISTRY, pos, state), EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancerSlots: IntArray = intArrayOf(6, 7, 8, 9)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.inventoryComponent = inventory(this) {
            input {
                slot = 1
                filter { (_, item), _ -> item is FishingRodItem }
            }
            output { slots = intArrayOf(2, 3, 4, 5) }
        }
    }

    private var cooldown = config.processSpeed
    
    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    override fun machineTick() {
        val upgrades = getEnhancers(inventoryComponent!!.inventory)
        if (!use(Enhancer.getEnergyCost(upgrades, this))) return
        val rodStack = inventoryComponent!!.inventory.getStack(1)
        if (rodStack.isEmpty || rodStack.item !is FishingRodItem) return
        cooldown += Enhancer.getSpeed(upgrades, this)
        if (cooldown < config.processSpeed) return
        cooldown = 0.0
        Direction.values().forEach { direction ->
            val pos = pos.offset(direction)
            if (world?.isWater(pos) == true) {
                val identifiers = getIdentifiers(tier)
                val id = identifiers[world!!.random!!.nextInt(identifiers.size)]
                val lootTable = (world as ServerWorld).server.lootManager.getTable(id)
                val ctx = LootContext.Builder(world as ServerWorld).random(world!!.random)
                    .parameter(LootContextParameters.ORIGIN, pos.toVec3d())
                    .parameter(LootContextParameters.TOOL, rodStack)
                    .build(LootContextTypes.FISHING)
                val loot = lootTable.generateLoot(ctx)
                loot.forEach { stack -> inventoryComponent?.inventory?.output(stack) }
                rodStack?.apply {
                    damage++
                    if (damage >= maxDamage) decrement(1)
                }
            }
        }
    }

    private fun getIdentifiers(tier: Tier) = when (tier) {
        Tier.MK2 -> arrayOf(FISH_IDENTIFIER)
        Tier.MK3 -> arrayOf(FISH_IDENTIFIER, FISH_IDENTIFIER, JUNK_IDENTIFIER, JUNK_IDENTIFIER, TREASURE_IDENTIFIER)
        else -> arrayOf(FISH_IDENTIFIER, FISH_IDENTIFIER, FISH_IDENTIFIER, TREASURE_IDENTIFIER)
    }

    override fun getEnergyCapacity(): Double = Enhancer.getBuffer(this)

    override fun getBaseValue(upgrade: Enhancer): Double = when (upgrade) {
        Enhancer.ENERGY -> config.energyCost
        Enhancer.SPEED -> 1.0
        Enhancer.BUFFER -> config.maxEnergyStored
        else -> 0.0
    }

    override fun getMaxCount(upgrade: Enhancer): Int {
        return if (upgrade == Enhancer.SPEED) return 4 else super.getMaxCount(upgrade)
    }

    companion object {
        private val FISH_IDENTIFIER = Identifier("gameplay/fishing/fish")
        private val JUNK_IDENTIFIER = Identifier("gameplay/fishing/junk")
        private val TREASURE_IDENTIFIER = Identifier("gameplay/fishing/treasure")
    }
}
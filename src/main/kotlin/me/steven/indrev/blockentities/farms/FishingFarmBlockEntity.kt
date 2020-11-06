package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.toVec3d
import net.minecraft.item.FishingRodItem
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class FishingFarmBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.FISHING_FARM_REGISTRY), UpgradeProvider {

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

    override fun machineTick() {
        val upgrades = getUpgrades(inventoryComponent!!.inventory)
        if (!Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))) return
        val rodStack = inventoryComponent!!.inventory.getStack(1)
        if (rodStack.isEmpty || rodStack.item !is FishingRodItem) return
        cooldown += Upgrade.getSpeed(upgrades, this)
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

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)

    override fun getUpgradeSlots(): IntArray = intArrayOf(6, 7, 8, 9)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> config.energyCost
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
    }

    companion object {
        private val FISH_IDENTIFIER = Identifier("gameplay/fishing/fish")
        private val JUNK_IDENTIFIER = Identifier("gameplay/fishing/junk")
        private val TREASURE_IDENTIFIER = Identifier("gameplay/fishing/treasure")
    }
}
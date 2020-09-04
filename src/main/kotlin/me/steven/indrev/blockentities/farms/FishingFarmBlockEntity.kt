package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.IConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
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

class FishingFarmBlockEntity(tier: Tier) : MachineBlockEntity(tier, MachineRegistry.FISHING_FARM_REGISTRY), UpgradeProvider {

    init {
        this.inventoryComponent = InventoryComponent {
            IRInventory(10, intArrayOf(1), intArrayOf(2, 3, 4, 5)) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is FishingRodItem -> slot == 1
                    else -> false
                }
            }
        }
    }

    private var cooldown = getConfig().processSpeed

    override fun machineTick() {
        val upgrades = getUpgrades(inventoryComponent!!.inventory)
        if (!Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))) return
        cooldown += Upgrade.getSpeed(upgrades, this)
        if (cooldown < getConfig().processSpeed) return
        cooldown = 0.0
        val rodStack = inventoryComponent?.inventory?.getStack(1)
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
                loot.forEach { stack -> inventoryComponent?.inventory?.addStack(stack) }
                rodStack?.apply {
                    damage++
                    if (damage >= maxDamage) decrement(1)
                }
            }
        }
    }

    override fun getBaseBuffer(): Double = getConfig().maxEnergyStored

    private fun getIdentifiers(tier: Tier) = when (tier) {
        Tier.MK2 -> arrayOf(FISH_IDENTIFIER)
        Tier.MK3 -> arrayOf(FISH_IDENTIFIER, JUNK_IDENTIFIER, TREASURE_IDENTIFIER)
        else -> arrayOf(FISH_IDENTIFIER, TREASURE_IDENTIFIER)
    }

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)

    override fun getUpgradeSlots(): IntArray = intArrayOf(6, 7, 8, 9)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> getConfig().energyCost
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> getBaseBuffer()
        else -> 0.0
    }

    fun getConfig(): IConfig {
        val machines = IndustrialRevolution.CONFIG.machines
        return when (tier) {
            Tier.MK2 -> machines.fishingMk2
            Tier.MK3 -> machines.fishingMk3
            else -> machines.fishingMk4
        }
    }

    companion object {
        private val FISH_IDENTIFIER = Identifier("gameplay/fishing/fish")
        private val JUNK_IDENTIFIER = Identifier("gameplay/fishing/junk")
        private val TREASURE_IDENTIFIER = Identifier("gameplay/fishing/treasure")
    }
}
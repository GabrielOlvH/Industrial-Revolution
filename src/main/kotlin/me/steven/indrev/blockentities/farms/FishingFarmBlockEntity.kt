package me.steven.indrev.blockentities.farms

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.MutableBlockPos
import me.steven.indrev.utils.Tier
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
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is FishingRodItem -> slot == 1
                    else -> false
                }
            }
        }
    }

    private var cooldown = getConfig().processSpeed

    override fun machineTick() {
        if (!Energy.of(this).use(Upgrade.ENERGY(this))) return
        cooldown--
        if (cooldown > 0) return
        cooldown = Upgrade.SPEED(this)
        val blockPos = MutableBlockPos(pos)
        val rodStack = inventoryComponent?.inventory?.getStack(1)
        Direction.values().forEach { direction ->
            blockPos.offset(direction)
            if (world?.isWater(blockPos) == true) {
                val id = IDENTIFIERS[world!!.random!!.nextInt(3)]
                val lootTable = (world as ServerWorld).server.lootManager.getTable(id)
                val ctx = LootContext.Builder(world as ServerWorld).random(world!!.random)
                    .parameter(LootContextParameters.POSITION, pos)
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

    override fun getMaxInput(side: EnergySide?): Double = getConfig().maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun getMaxStoredPower(): Double = getConfig().maxEnergyStored

    override fun getUpgradeSlots(): IntArray = intArrayOf(6, 7, 8, 9)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.ALL

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> getConfig().energyCost + Upgrade.SPEED(this)
        Upgrade.SPEED -> getConfig().processSpeed
        Upgrade.BUFFER -> getBaseBuffer()
    }

    fun getConfig(): MachineConfig {
        val machines = IndustrialRevolution.CONFIG.machines
        return when (tier) {
            Tier.MK2 -> machines.fishing_mk2
            Tier.MK3 -> machines.fishing_mk3
            else -> machines.fishing_mk4
        }
    }

    companion object {
        private val FISH_IDENTIFIER = Identifier("gameplay/fishing/fish")
        private val JUNK_IDENTIFIER = Identifier("gameplay/fishing/junk")
        private val TREASURE_IDENTIFIER = Identifier("gameplay/fishing/treasure")
        private val IDENTIFIERS = arrayOf(FISH_IDENTIFIER, JUNK_IDENTIFIER, TREASURE_IDENTIFIER)
    }
}
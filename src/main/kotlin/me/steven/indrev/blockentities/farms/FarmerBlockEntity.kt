package me.steven.indrev.blockentities.farms

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.map
import me.steven.indrev.utils.toVec3d
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.BoneMealItem
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

class FarmerBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.FARMER_REGISTRY), UpgradeProvider {

    init {
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(1, 2, 3, 4) }
            output { slots = intArrayOf(5, 6, 7, 8, 9, 10, 11, 12, 13) }
        }
    }

    override var range: Int = 5

    var cooldown = 0.0
    var nextBlocks = mutableListOf<BlockPos>().iterator()

    override fun machineTick() {
        val inventory = inventoryComponent?.inventory ?: return
        val upgrades = getUpgrades(inventory)
        cooldown += Upgrade.getSpeed(upgrades, this)
        if (cooldown < config.processSpeed) return
        val world = world as ServerWorld
        val energyCost = Upgrade.getEnergyCost(upgrades, this)
        if (!canUse(energyCost)) return
        if (nextBlocks.hasNext()) {
            var pos = nextBlocks.next()
            var state = world.getBlockState(pos)
            while (nextBlocks.hasNext()) {
                if (state.block != Blocks.SUGAR_CANE)
                    tryHarvest(state, pos, world)
                else {
                    for (i in 5 downTo 1) {
                        val posToHarvest = pos.up(i)
                        val toHarvest = world.getBlockState(posToHarvest)
                        if (toHarvest.block == Blocks.SUGAR_CANE) {
                            tryHarvest(state, posToHarvest, world)
                        }
                    }
                }
                pos = nextBlocks.next()
                state = world.getBlockState(pos)
            }
        } else {
            nextBlocks = getWorkingArea().map(::BlockPos).iterator()
        }
        cooldown = 0.0
    }

    fun tryHarvest(state: BlockState, pos: BlockPos, world: ServerWorld): Boolean {
        val block = state.block

        val inventory = inventoryComponent?.inventory
        val performedAction = inventory?.inputSlots?.any { slot ->
            val stack = inventory.getStack(slot)
            val item = stack.item
            val isValidSeed = item is BlockItem && (item.block is CropBlock || item.block is StemBlock || item.block == Blocks.SUGAR_CANE)
            val isCropBlock = block is CropBlock || block is StemBlock
            val shouldHarvest =
                (block is CropBlock && block.isMature(state) && (item is BlockItem && item.block == block || slot == 4)) || block is GourdBlock || block == Blocks.SUGAR_CANE
            when {
                item is BoneMealItem && isCropBlock && (block as Fertilizable).isFertilizable(world, pos, state, false) -> {
                    stack.decrement(1)
                    (block as Fertilizable).grow(world, world.random, pos, state)
                    world.syncWorldEvent(2005, pos, 0)
                    true
                }
                shouldHarvest -> {
                    if (block is CropBlock && stack.count > 1) {
                        world.setBlockState(pos, block.defaultState)
                        stack.decrement(1)
                    } else {
                        world.setBlockState(pos, Blocks.AIR.defaultState)
                    }
                    val lootTable = world.server.lootManager.getTable(block.lootTableId)
                    val lootContext = LootContext.Builder(world)
                        .random(world.random)
                        .parameter(LootContextParameters.ORIGIN, pos.toVec3d())
                        .parameter(LootContextParameters.BLOCK_STATE, state)
                        .parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
                        .build(LootContextTypes.BLOCK)
                    lootTable.generateLoot(lootContext).forEach { inventory.output(it) }
                    true
                }
                block == Blocks.AIR && isValidSeed && stack.count > 1 -> {
                    val cropState = (item as BlockItem).block.defaultState
                    if (cropState.canPlaceAt(world, pos) && world.isAir(pos)) {
                        world.setBlockState(pos, cropState)
                        stack.count--
                        true
                    } else false
                }
                else -> false
            }
        } ?: false
        if (performedAction)
            use(Upgrade.getEnergyCost(getUpgrades(inventory!!), this))
        return performedAction
    }

    override fun getWorkingArea(): Box = Box(pos).expand(range.toDouble(), 0.0, range.toDouble())

    override fun getUpgradeSlots(): IntArray = intArrayOf(14, 15, 16, 17)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getBaseValue(upgrade: Upgrade): Double = when (upgrade) {
        Upgrade.ENERGY -> config.energyCost
        Upgrade.SPEED -> 1.0
        Upgrade.BUFFER -> config.maxEnergyStored
        else -> 0.0
    }
}
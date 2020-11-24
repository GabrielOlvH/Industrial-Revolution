package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.toVec3d
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.block.LeavesBlock
import net.minecraft.item.AxeItem
import net.minecraft.item.BlockItem
import net.minecraft.item.BoneMealItem
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.BlockTags
import net.minecraft.tag.ItemTags
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.chunk.WorldChunk
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class ChopperBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHOPPER_REGISTRY), UpgradeProvider {
    init {
        this.inventoryComponent = inventory(this) {
            input {
                slots = intArrayOf(2, 3, 4, 5)
                2 filter { (_, item) -> item.isIn(FabricToolTags.AXES) }
                3 filter { (_, item) -> item is BoneMealItem }
                4..5 filter { (_, item), _ -> item.isIn(ItemTags.SAPLINGS) }
            }
            output { slots = intArrayOf(6, 7, 8, 9, 10, 11, 12, 13, 14) }
            coolerSlot = 1
        }
    }

    private var scheduledBlocks = mutableListOf<BlockPos>().iterator()
    override var range = 5
    var cooldown = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val upgrades = getUpgrades(inventory)
        cooldown += Upgrade.getSpeed(upgrades, this)
        if (cooldown < config.processSpeed || !Energy.of(this).simulate().use(Upgrade.getEnergyCost(upgrades, this)))
            return
        if (!scheduledBlocks.hasNext()) {
            val list = mutableListOf<BlockPos>()
            val area = getWorkingArea()
            for (y in area.minY.toInt() until area.maxY.toInt()) {
                for (x in area.minX.toInt() until area.maxX.toInt()) {
                    for (z in area.minZ.toInt() until area.maxZ.toInt()) {
                        list.add(BlockPos(x, y, z))
                    }
                }
            }
            scheduledBlocks = list.iterator()
        } else {
            var currentChunk: Chunk? = null
            var currentSection: ChunkSection? = null
            var performedAction = false
            val axeStack = inventory.inputSlots.map { slot -> inventory.getStack(slot) }.firstOrNull { stack -> stack.item is AxeItem }
            outer@ while (scheduledBlocks.hasNext()) {
                val pos = scheduledBlocks.next()
                if (pos.x shr 4 != currentChunk?.pos?.x || pos.z shr 4 != currentChunk.pos.z) {
                    currentChunk = world?.getChunk(pos)
                    currentSection = currentChunk!!.sectionArray[pos.y shr 4]
                    if (currentSection == WorldChunk.EMPTY_SECTION) {
                        currentSection = ChunkSection(pos.y shr 4 shl 4)
                        currentChunk.sectionArray[pos.y shr 4] = currentSection
                    }
                }
                val blockState = currentSection?.getBlockState(pos.x and 15, pos.y and 15, pos.z and 15) ?: continue
                if (axeStack != null
                    && !axeStack.isEmpty
                    && tryChop(axeStack, pos, blockState, inventory)
                ) {
                    performedAction = true
                    break
                }
                for (slot in inventory.inputSlots) {
                    val stack = inventory.getStack(slot)
                    if (
                        (axeStack != null && stack.isItemEqual(axeStack))
                        || stack.isEmpty
                        || !tryUse(blockState, stack, pos)
                    ) continue
                    performedAction = true
                    break@outer
                }
            }
            if (performedAction) Energy.of(this).use(Upgrade.getEnergyCost(upgrades, this))
            temperatureComponent?.tick(performedAction)
            workingState = performedAction
        }
        cooldown = 0.0
    }

    private fun tryChop(
        axeStack: ItemStack,
        blockPos: BlockPos,
        blockState: BlockState,
        inventory: IRInventory
    ): Boolean {
        val block = blockState.block
        when {
            block.isIn(BlockTags.LOGS) -> {
                if (Energy.valid(axeStack) && !Energy.of(axeStack).use(1.0))
                    return false
                else {
                    axeStack.damage(1, world?.random, null)
                    if (axeStack.damage >= axeStack.maxDamage)
                        axeStack.decrement(1)
                }
                world?.breakBlock(blockPos, false)
            }
            block is LeavesBlock -> {
                world?.breakBlock(blockPos, false)
            }
            else -> return false
        }
        val droppedStacks = blockState.getDroppedStacks(
            LootContext.Builder(world as ServerWorld).random(world?.random)
                .parameter(LootContextParameters.ORIGIN, blockPos.toVec3d())
                .parameter(LootContextParameters.TOOL, axeStack)
        )
        droppedStacks.forEach {
            if (!inventory.output(it))
                ItemScatterer.spawn(world, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), it)
        }
        return true
    }

    private fun tryUse(blockState: BlockState, itemStack: ItemStack, pos: BlockPos): Boolean {
        val item = itemStack.item
        val block = blockState.block
        when {
            item is BoneMealItem && itemStack.count > 1
                    && block.isIn(BlockTags.SAPLINGS)
                    && block is Fertilizable
                    && block.isFertilizable(world, pos, blockState, false)
                    && block.canGrow(world, world?.random, pos, blockState) -> {
                block.grow(world as ServerWorld, world?.random, pos, blockState)
                world?.syncWorldEvent(2005, pos, 0)
                itemStack.decrement(1)
            }
            item.isIn(ItemTags.SAPLINGS) && item is BlockItem && item.block.defaultState.canPlaceAt(world, pos) && itemStack.count > 1 -> {
                if (world?.isAir(pos) == true) {
                    world?.setBlockState(pos, item.block.defaultState, 3)
                    itemStack.decrement(1)
                } else return false
            }
            else -> return false
        }
        return true
    }

    override fun getUpgradeSlots(): IntArray = intArrayOf(15, 16, 17, 18)

    override fun getAvailableUpgrades(): Array<Upgrade> = Upgrade.DEFAULT

    override fun getBaseValue(upgrade: Upgrade): Double =
        when (upgrade) {
            Upgrade.ENERGY -> config.energyCost
            Upgrade.SPEED -> 1.0
            Upgrade.BUFFER -> getBaseBuffer()
            else -> 0.0
        }

    override fun getMaxStoredPower(): Double = Upgrade.getBuffer(this)

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0
}
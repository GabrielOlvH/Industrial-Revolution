package me.steven.indrev.blockentities.farms

import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.components.InventoryComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.items.misc.IRCoolerItem
import me.steven.indrev.items.upgrade.IRUpgradeItem
import me.steven.indrev.items.upgrade.Upgrade
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.FakePlayerEntity
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.toIntArray
import me.steven.indrev.utils.toVec3d
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.BlockState
import net.minecraft.block.LeavesBlock
import net.minecraft.block.SaplingBlock
import net.minecraft.item.*
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.BlockTags
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.chunk.Chunk
import net.minecraft.world.chunk.ChunkSection
import net.minecraft.world.chunk.WorldChunk
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class ChopperBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHOPPER_REGISTRY), UpgradeProvider {
    init {
        this.inventoryComponent = InventoryComponent({ this }) {
            IRInventory(19, (2..5).toIntArray(), (6 until 15).toIntArray()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRUpgradeItem -> getUpgradeSlots().contains(slot)
                    Energy.valid(stack) && Energy.of(stack).maxOutput > 0 -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    slot in 2..5 -> item?.isIn(FabricToolTags.AXES) == true || item is BoneMealItem || (item is BlockItem && item.block is SaplingBlock)
                    slot in 6 until 15 -> true
                    else -> false
                }
            }
        }
    }

    private var scheduledBlocks = mutableListOf<BlockPos>().iterator()
    private val fakePlayer by lazy { FakePlayerEntity(world!!, pos) }
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
            setWorkingState(performedAction)
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
        droppedStacks.map { inventory.addStack(it) }.filterNot(ItemStack::isEmpty).forEach {
            ItemScatterer.spawn(world, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), it)
        }
        return true
    }

    private fun tryUse(blockState: BlockState, itemStack: ItemStack, pos: BlockPos): Boolean {
        fakePlayer.setStackInHand(Hand.MAIN_HAND, itemStack)
        val item = itemStack.item
        val isSaplingOrBoneMeal = (item is BoneMealItem && blockState.block is SaplingBlock && itemStack.count > 1) || (item is BlockItem && item.block is SaplingBlock)
        if (!isSaplingOrBoneMeal) return false
        val useResult = itemStack.useOnBlock(
            ItemUsageContext(
                fakePlayer,
                Hand.MAIN_HAND,
                BlockHitResult(pos.toVec3d(), Direction.UP, pos, false)
            )
        )
        fakePlayer.inventory.clear()
        return useResult.isAccepted
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
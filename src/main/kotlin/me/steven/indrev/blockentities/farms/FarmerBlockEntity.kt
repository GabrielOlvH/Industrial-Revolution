package me.steven.indrev.blockentities.farms

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.map
import me.steven.indrev.utils.toVec3d
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.BoneMealItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

class FarmerBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.FARMER_REGISTRY), EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancementsSlots: IntArray = intArrayOf(14, 15, 16, 17)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.inventoryComponent = inventory(this) {
            input { slots = intArrayOf(1, 2, 3, 4) }
            output { slots = intArrayOf(5, 6, 7, 8, 9, 10, 11, 12, 13) }
        }
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    override var range: Int = 5

    var cooldown = 0.0
    var nextBlocks = mutableListOf<BlockPos>().iterator()

    override fun machineTick() {
        val inventory = inventoryComponent?.inventory ?: return
        val enhancements = getEnhancers(inventory)
        cooldown += Enhancer.getSpeed(enhancements, this)
        if (cooldown < config.processSpeed) return
        val world = world as ServerWorld
        val energyCost = Enhancer.getEnergyCost(enhancements, this)
        if (!canUse(energyCost)) return
        if (nextBlocks.hasNext()) {
            while (nextBlocks.hasNext()) {
                val pos = nextBlocks.next()
                val state = world.getBlockState(pos)
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
            }
        } else {
            nextBlocks = getWorkingArea().map(::BlockPos).iterator()
        }
        cooldown = 0.0
    }

    private fun tryHarvest(state: BlockState, pos: BlockPos, world: ServerWorld): Boolean {
        val block = state.block

        val inventory = inventoryComponent?.inventory
        val performedAction = inventory?.inputSlots?.any { slot ->
            val stack = inventory.getStack(slot)
            val item = stack.item
            val isCropBlock = block is CropBlock || block is StemBlock || block is SweetBerryBushBlock || block is CocoaBlock
            when {
                item is BoneMealItem && isCropBlock && (block as Fertilizable).isFertilizable(world, pos, state, false) && block.canGrow(world, world.random, pos, state) -> {
                    stack.decrement(1)
                    block.grow(world, world.random, pos, state)
                    world.syncWorldEvent(2005, pos, 0)
                    true
                }
                canHarvest(slot, state, block, item) -> {
                    if ((block is CropBlock || block is SweetBerryBushBlock) && stack.count > 1) {
                        world.setBlockState(pos, block.defaultState)
                        stack.decrement(1)
                    } else {
                        world.setBlockState(pos, Blocks.AIR.defaultState)
                    }
                    val droppedStacks = state.getDroppedStacks(LootContext.Builder(world)
                        .random(world.random)
                        .parameter(LootContextParameters.ORIGIN, pos.toVec3d())
                        .parameter(LootContextParameters.BLOCK_STATE, state)
                        .parameter(LootContextParameters.TOOL, ItemStack.EMPTY))
                    droppedStacks.forEach { inventory.output(it) }
                    true
                }
                block is AirBlock && canPlant(item) && stack.count > 1 -> {
                    var cropState = (item as BlockItem).block.defaultState

                    if (item.block is CocoaBlock) {
                        arrayOf(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).firstOrNull {
                            cropState = cropState.with(CocoaBlock.FACING, it)
                            cropState.canPlaceAt(world, pos)
                        } ?: return@any false
                    }

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
            use(Enhancer.getEnergyCost(getEnhancers(inventory!!), this))
        return performedAction
    }

    private fun canPlant(item: Item) =
        item is BlockItem && (
                item.block is CropBlock
                        || item.block is StemBlock
                        || item.block == Blocks.SUGAR_CANE
                        || item.block is SweetBerryBushBlock
                        || item.block is CocoaBlock
                )

    private fun canHarvest(slot: Int, state: BlockState, block: Block, item: Item): Boolean =
        (((block is CropBlock && block.isMature(state))
                || (block is SweetBerryBushBlock && state[SweetBerryBushBlock.AGE] == 2))
                && (item is BlockItem && item.block == block || slot == 4))
                || block is GourdBlock
                || block == Blocks.SUGAR_CANE
                || (block is CocoaBlock && state[CocoaBlock.AGE] == 2)

    override fun getWorkingArea(): Box = Box(pos).expand(range.toDouble(), 0.0, range.toDouble())

    override fun getMaxEnhancer(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 1 else super.getMaxEnhancer(enhancer)
    }

    override fun getBaseValue(enhancer: Enhancer): Double = when (enhancer) {
        Enhancer.ENERGY -> config.energyCost
        Enhancer.SPEED -> 1.0
        Enhancer.BUFFER -> config.maxEnergyStored
        else -> 0.0
    }

    override fun getEnergyCapacity(): Double = Enhancer.getBuffer(this)
}
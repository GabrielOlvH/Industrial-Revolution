package me.steven.indrev.blockentities.farms

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.crafters.EnhancerProvider
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.enhancer.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags
import net.minecraft.block.*
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
import net.minecraft.util.math.Box
import net.minecraft.world.chunk.Chunk

class ChopperBlockEntity(tier: Tier) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHOPPER_REGISTRY), EnhancerProvider {

    override val backingMap: Object2IntMap<Enhancer> = Object2IntArrayMap()
    override val enhancementsSlots: IntArray = intArrayOf(15, 16, 17, 18)
    override val availableEnhancers: Array<Enhancer> = Enhancer.DEFAULT

    init {
        this.inventoryComponent = inventory(this) {
            input {
                slots = intArrayOf(2, 3, 4, 5)
                2 filter { (_, item) -> item.isIn(FabricToolTags.AXES) || item.isIn(FabricToolTags.SWORDS) }
                3 filter { (_, item) -> item is BoneMealItem }
                4..5 filter { (_, item), _ -> item.isIn(ItemTags.SAPLINGS)
                        || (item is BlockItem && (item.block is MushroomPlantBlock || item.block is BambooBlock)) }
            }
            output { slots = intArrayOf(6, 7, 8, 9, 10, 11, 12, 13, 14) }
            coolerSlot = 1
        }
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    private var scheduledBlocks = mutableListOf<BlockPos>().iterator()
    private val scannedBlocks = mutableSetOf<BlockPos>()
    override var range = 5
    var cooldown = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        val enhancements = getEnhancers(inventory)
        cooldown += Enhancer.getSpeed(enhancements, this)
        val energyCost = Enhancer.getEnergyCost(enhancements, this)
        if (cooldown < config.processSpeed || ticks % 15 != 0 || !canUse(energyCost))
            return
        val area = getWorkingArea()
        if (!scheduledBlocks.hasNext()) {
            // includes tree branches that goes outside the actual area
            val fullArea = area.expand(4.0).shrink(0.0, -4.0, 0.0)
            scheduledBlocks = fullArea.map(::BlockPos).iterator()
            scannedBlocks.clear()
        } else {
            var currentChunk: Chunk? = null
            var performedActions = 0
            val axeStack = inventory.getStack(2)
            val brokenBlocks = hashMapOf<BlockPos, BlockState>()
            while (scheduledBlocks.hasNext() && cooldown >= config.processSpeed) {
                val pos = scheduledBlocks.next()
                if (!scannedBlocks.add(pos)) continue
                if (pos.x shr 4 != currentChunk?.pos?.x || pos.z shr 4 != currentChunk.pos.z) {
                    currentChunk = world?.getChunk(pos)
                }
                val blockState = currentChunk?.getBlockState(pos) ?: continue
                if (axeStack != null
                    && !axeStack.isEmpty
                    && tryChop(axeStack, pos, blockState, currentChunk)
                ) {
                    cooldown -= config.processSpeed
                    if (!use(energyCost)) break
                    brokenBlocks[pos] = blockState
                    performedActions++
                }
                if (pos.y == this.pos.y && pos in area) {
                    for (slot in 3..5) {
                        val stack = inventory.getStack(slot)
                        if (stack.isEmpty || !tryUse(blockState, stack, pos)) continue
                        cooldown -= config.processSpeed
                        if (!use(energyCost)) break
                        brokenBlocks[pos] = blockState
                        performedActions++
                    }
                }
            }
            brokenBlocks.forEach { (blockPos, blockState) ->
                val droppedStacks = blockState.getDroppedStacks(
                    LootContext.Builder(world as ServerWorld).random(world?.random)
                        .parameter(LootContextParameters.ORIGIN, blockPos.toVec3d())
                        .parameter(LootContextParameters.TOOL, axeStack)
                )
                droppedStacks.forEach {
                    if (!inventory.output(it))
                        ItemScatterer.spawn(world, blockPos.x.toDouble(), blockPos.y.toDouble(), blockPos.z.toDouble(), it)
                }
            }
            temperatureComponent?.tick(performedActions > 0)
            workingState = performedActions > 0
        }
        cooldown = 0.0
    }

    private fun tryChop(
        toolStack: ItemStack,
        blockPos: BlockPos,
        blockState: BlockState,
        chunk: Chunk
    ): Boolean {
        fun damageTool(amount: Int): Boolean {
            return when {
                energyOf(toolStack).let { it != null && !it.use(amount.toDouble()) } -> false
                toolStack.isEmpty -> false
                else -> {
                    toolStack.damage(amount, world?.random, null)
                    if (toolStack.damage >= toolStack.maxDamage)
                        toolStack.decrement(1)
                    true
                }
            }
        }
        val block = blockState.block
        when {
            toolStack.item.isIn(FabricToolTags.AXES)
                    && (block.isIn(BlockTags.LOGS) || block is MushroomBlock || block == Blocks.MUSHROOM_STEM) -> {
                if (!damageTool(1)) return false
                world?.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
            }
            block is LeavesBlock -> {
                world?.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
            }
            toolStack.item.isIn(FabricToolTags.SWORDS) && block is BambooBlock && blockPos.y > pos.y -> {
                val upPos = blockPos.up()
                val up = chunk.getBlockState(upPos)
                scannedBlocks.add(upPos)
                if (up.isOf(block))
                    tryChop(toolStack, upPos, blockState, chunk)
                if (!damageTool(2)) return false
                world?.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
            }
            else -> return false
        }
        return true
    }

    private fun tryUse(blockState: BlockState, itemStack: ItemStack, pos: BlockPos): Boolean {
        val item = itemStack.item
        val block = blockState.block
        when {
            item is BoneMealItem && itemStack.count > 1
                    && (block.isIn(BlockTags.SAPLINGS) || block is MushroomPlantBlock || block is BambooBlock || block is BambooSaplingBlock)
                    && block is Fertilizable
                    && block.isFertilizable(world, pos, blockState, false)
                    && block.canGrow(world, world?.random, pos, blockState) -> {
                block.grow(world as ServerWorld, world?.random, pos, blockState)
                world?.syncWorldEvent(2005, pos, 0)
                itemStack.decrement(1)
            }
            block is AirBlock
                    && item is BlockItem
                    && (item.isIn(ItemTags.SAPLINGS) || item.block is MushroomPlantBlock || item.block is BambooBlock)
                    && item.block.defaultState.canPlaceAt(world, pos)
                    && itemStack.count > 1 -> {
                if (item.block is BambooBlock)
                    world?.setBlockState(pos, Blocks.BAMBOO_SAPLING.defaultState, 3)
                else
                    world?.setBlockState(pos, item.block.defaultState, 3)
                itemStack.decrement(1)
            }
            else -> return false
        }
        return true
    }

    override fun getBaseValue(enhancer: Enhancer): Double =
        when (enhancer) {
            Enhancer.ENERGY -> config.energyCost
            Enhancer.SPEED -> 1.0
            Enhancer.BUFFER -> config.maxEnergyStored
            else -> 0.0
        }

    override fun getWorkingArea(): Box {
        val box = Box(pos)
        return box.expand(range.toDouble(), 0.0, range.toDouble()).stretch(0.0, 40.0, 0.0)
    }

    override fun getMaxEnhancer(enhancer: Enhancer): Int {
        return if (enhancer == Enhancer.SPEED) return 12 else super.getMaxEnhancer(enhancer)
    }

    override fun getEnergyCapacity(): Double = Enhancer.getBuffer(this)
}
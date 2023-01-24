package me.steven.indrev.blockentities.farms

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.components.EnhancerComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.*
import net.minecraft.block.*
import net.minecraft.item.AxeItem
import net.minecraft.item.BlockItem
import net.minecraft.item.BoneMealItem
import net.minecraft.item.ItemStack
import net.minecraft.item.SwordItem
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.BlockTags
import net.minecraft.tag.ItemTags
import net.minecraft.util.ItemScatterer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.world.chunk.Chunk

class ChopperBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : AOEMachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.CHOPPER_REGISTRY, pos, state) {
    
    init {
        this.enhancerComponent = EnhancerComponent(intArrayOf(15, 16, 17, 18), Enhancer.DEFAULT, this::getMaxCount)
        this.inventoryComponent = inventory(this) {
            input {
                slots = intArrayOf(2, 3, 4, 5)
                //todo tool tags?
                2 filter { stack -> stack.item is AxeItem || stack.item is SwordItem }
                3 filter { (_, item) -> item is BoneMealItem }
                4..5 filter { (stack, item), _ -> stack.isIn(ItemTags.SAPLINGS)
                        || (item is BlockItem && (item.block is MushroomPlantBlock || item.block is BambooBlock || item.block is FungusBlock)) }
            }
            output { slots = intArrayOf(6, 7, 8, 9, 10, 11, 12, 13, 14) }
            coolerSlot = 1
        }
    }

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    private var scheduledBlocks = mutableListOf<BlockPos>().iterator()
    private val scannedBlocks = mutableSetOf<BlockPos>()
    override var range = 5
    var cooldown = 0.0

    override fun machineTick() {
        if (world?.isClient == true) return
        val inventory = inventoryComponent?.inventory ?: return
        cooldown += getProcessingSpeed()
        if (cooldown < config.processSpeed || ticks % 15 != 0 || !canUse(getEnergyCost()))
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
                    if (!use(getEnergyCost())) break
                    brokenBlocks[pos] = blockState
                    performedActions++
                }
                if (pos.y == this.pos.y && pos in area) {
                    for (slot in 3..5) {
                        val stack = inventory.getStack(slot)
                        if (stack.isEmpty || !tryUse(blockState, stack, pos)) continue
                        cooldown -= config.processSpeed
                        if (!use(getEnergyCost())) break
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
                energyOf(toolStack) != null -> energyOf(toolStack)!!.use(amount.toLong())
                toolStack.isEmpty -> false
                toolStack.isDamageable -> {
                    toolStack.damage(amount, world?.random, null)
                    if (toolStack.damage >= toolStack.maxDamage)
                        toolStack.decrement(1)
                    true
                }
                else -> true
            }
        }
        val block = blockState.block
        when {
            toolStack.item is AxeItem && block !is FungusBlock
                    && (blockState.isIn(BlockTags.LOGS) || block is MushroomBlock || block == Blocks.MUSHROOM_STEM ||
                    block == Blocks.SHROOMLIGHT || block is NetherWartBlock || block == Blocks.WARPED_WART_BLOCK || block is RootsBlock || block is WeepingVinesBlock || block is WeepingVinesPlantBlock ||
                    block is TwistingVinesPlantBlock || block is TwistingVinesBlock ||
                    block.defaultState?.material == Material.NETHER_SHOOTS || block.defaultState?.material == Material.NETHER_WOOD) -> {
                if (!damageTool(1)) return false
                world?.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
            }
            block is LeavesBlock -> {
                world?.setBlockState(blockPos, Blocks.AIR.defaultState, 3)
            }
            toolStack.item is SwordItem && block is BambooBlock && blockPos.y > pos.y -> {
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
                    && (blockState.isIn(BlockTags.SAPLINGS) || block is MushroomPlantBlock || block is BambooBlock || block is BambooSaplingBlock)
                    && block is Fertilizable
                    && block.isFertilizable(world, pos, blockState, false)
                    && block.canGrow(world, world?.random, pos, blockState) -> {
                block.grow(world as ServerWorld, world?.random, pos, blockState)
                world?.syncWorldEvent(2005, pos, 0)
                itemStack.decrement(1)
            }
            block is AirBlock
                    && item is BlockItem
                    && (itemStack.isIn(ItemTags.SAPLINGS) || item.block is MushroomPlantBlock || item.block is BambooBlock || item.block is FungusBlock)
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

    override fun getEnergyCost(): Long {
        val speedEnhancers = (enhancerComponent!!.getCount(Enhancer.SPEED) * 2).coerceAtLeast(1)
        return config.energyCost * speedEnhancers
    }

    override fun getWorkingArea(): Box {
        val box = Box(pos)
        return box.expand(range.toDouble(), 0.0, range.toDouble()).stretch(0.0, 40.0, 0.0)
    }
    
    fun getMaxCount(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED -> return 12
            Enhancer.BUFFER -> 4
            else -> 1
        }
    }
}

package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.MachineItemInventory
import me.steven.indrev.utils.Directions
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.minecraft.block.Block
import net.minecraft.block.BlockEntityProvider
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.state.StateManager
import net.minecraft.state.property.DirectionProperty
import net.minecraft.state.property.Properties
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import team.reborn.energy.api.EnergyStorage

class MachineBlock(settings: Settings, private val blockEntityFactory: (BlockPos, BlockState) -> BlockEntity) : Block(settings), BlockEntityProvider {

    init {
        ItemStorage.SIDED.registerForBlocks({ _, _, _, be, dir ->
            val blockEntity = be as? MachineBlockEntity<*> ?: return@registerForBlocks null
            return@registerForBlocks if (blockEntity.inventory.exists())
                blockEntity.inventory.getSide(dir)
            else
                null
        }, this)

        EnergyStorage.SIDED.registerForBlocks({ _, _, _, be, dir ->
            val blockEntity = be as? MachineBlockEntity<*> ?: return@registerForBlocks null
            return@registerForBlocks blockEntity.energyInventories[dir!!.id]
        }, this)

        this.defaultState = this.stateManager.defaultState.with(FACING, Direction.NORTH)
    }

    private var cachedItems: Array<MachineBlockItem>? = null

    override fun asItem(): Item {
        if (cachedItems == null) {
            cachedItems = MACHINES[Registries.BLOCK.getId(this)]?.blockItems
        }
        return cachedItems!![0]
    }

    override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
        if (cachedItems == null) {
            cachedItems = MACHINES[Registries.BLOCK.getId(this)]?.blockItems
        }
        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return ItemStack(cachedItems!![0])
        return ItemStack(cachedItems!![blockEntity.tier.ordinal % cachedItems!!.size])
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(FACING, ctx.horizontalPlayerFacing.opposite)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity,
        hand: Hand,
        hit: BlockHitResult
    ): ActionResult {
        if (world.isClient) return ActionResult.SUCCESS
        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return ActionResult.PASS
        return blockEntity.onUse(state, world, pos, player, hand, hit)
    }

    override fun onStateReplaced(
        state: BlockState,
        world: World,
        pos: BlockPos,
        newState: BlockState,
        moved: Boolean
    ) {
        if (!state.isOf(newState.block)) {
            val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*>
            if (blockEntity != null && blockEntity.inventory.exists()) {
                dropInventory(world, blockEntity.inventory, pos.x, pos.y, pos.z)
            }
        }
        super.onStateReplaced(state, world, pos, newState, moved)
    }

    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        sourcePos: BlockPos,
        notify: Boolean
    ) {
        super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify)
        if (!world.isClient) {
            val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
            blockEntity.connectedEnergyInventories = Directions.ALL
        }
    }

    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        super.randomDisplayTick(state, world, pos, random)
        val blockEntity = world.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
        blockEntity.randomDisplayTick(state, world, pos, random)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = blockEntityFactory(pos, state)

    override fun <T : BlockEntity?> getTicker(
        world: World,
        state: BlockState,
        type: BlockEntityType<T>
    ): BlockEntityTicker<T> {
        return BlockEntityTicker { _, _, _, blockEntity -> (blockEntity as? MachineBlockEntity<*>)?.tick() }
    }

    private fun dropInventory(world: World, inventory: MachineItemInventory, x: Int, y: Int, z: Int) {
        (0 until inventory.size).forEach { index ->
            val slot = inventory[index]
            ItemScatterer.spawn(world, x.toDouble(), y.toDouble(), z.toDouble(), slot.resource.toStack(slot.amount.toInt()))
            inventory.parts[index].variant = ItemVariant.blank()
            inventory.parts[index].amount = 0
        }
    }

    companion object {
        val FACING: DirectionProperty = Properties.HORIZONTAL_FACING
    }
}
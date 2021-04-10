package me.steven.indrev.blocks.machine.pipes

import alexiil.mc.lib.attributes.item.impl.EmptyGroupedItemInv
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.config.IRConfig
import me.steven.indrev.gui.controllers.pipes.PipeFilterController
import me.steven.indrev.gui.controllers.pipes.PipeFilterScreenFactory
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.ServoNetworkState
import me.steven.indrev.networks.item.ItemNetworkState
import me.steven.indrev.utils.groupedItemInv
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.Text
import net.minecraft.text.TranslatableText
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class ItemPipeBlock(settings: Settings, tier: Tier) : BasePipeBlock(settings, tier, Network.Type.ITEM) {
    override fun appendTooltip(
        stack: ItemStack?,
        world: BlockView?,
        tooltip: MutableList<Text>?,
        options: TooltipContext?
    ) {
        tooltip?.add(
            TranslatableText("gui.indrev.tooltip.maxTransferRate").formatted(Formatting.AQUA)
                .append(TranslatableText("gui.indrev.tooltip.itemsec", getMaxTransferRate()).formatted(Formatting.GRAY))
        )
    }

    override fun getOutlineShape(
        state: BlockState,
        view: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape {
        return if (state[COVERED]) VoxelShapes.fullCube()
        else getShape(state)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos,
        player: PlayerEntity?,
        hand: Hand?,
        hit: BlockHitResult
    ): ActionResult {
        val dir = getSideFromHit(hit.pos, pos)
        if (hand == Hand.MAIN_HAND && !world.isClient && player!!.getStackInHand(hand).isEmpty && dir != null && state[getProperty(dir)]) {
            val state = Network.Type.ITEM.getNetworkState(world as ServerWorld) as ItemNetworkState
            if (state[pos]?.containers?.containsKey(pos.offset(dir)) == true) {
                player.openHandledScreen(PipeFilterScreenFactory(::PipeFilterController, pos, dir))
                return ActionResult.SUCCESS
            }
        }
        return super.onUse(state, world, pos, player, hand, hit)
    }

    override fun isConnectable(world: ServerWorld, pos: BlockPos, dir: Direction) =
        groupedItemInv(world, pos, dir) != EmptyGroupedItemInv.INSTANCE
                || world.getBlockState(pos).block.let { it is ItemPipeBlock && it.tier == tier }
                || (type.getNetworkState(world) as ServoNetworkState<*>).hasServo(pos.offset(dir), dir.opposite)


    private fun getMaxTransferRate() = when(tier) {
        Tier.MK1 -> IRConfig.cables.itemPipeMk1
        Tier.MK2 -> IRConfig.cables.itemPipeMk2
        Tier.MK3 -> IRConfig.cables.itemPipeMk3
        else -> IRConfig.cables.itemPipeMk4
    }

    override fun getShape(blockState: BlockState): VoxelShape {
        val directions = Direction.values().filter { dir -> blockState[getProperty(dir)] }.toTypedArray()
        var cableShapeCache = SHAPE_CACHE.firstOrNull { shape -> shape.directions.contentEquals(directions) }
        if (cableShapeCache == null) {
            var shape = CENTER_SHAPE
            Direction.values().forEach { direction ->
                if (blockState[getProperty(direction)]) shape = VoxelShapes.union(shape, getShape(direction))
            }
            cableShapeCache = PipeShape(directions, shape)
            SHAPE_CACHE.add(cableShapeCache)
        }
        return cableShapeCache.shape
    }

    companion object {

        val SHAPE_CACHE = hashSetOf<PipeShape>()

        val DOWN_SHAPE: VoxelShape = createCuboidShape(6.5, 0.0, 6.5, 9.5, 6.5, 9.5)
        val UP_SHAPE: VoxelShape = createCuboidShape(6.5, 9.5, 6.5, 9.5, 16.0, 9.5)
        val SOUTH_SHAPE: VoxelShape = createCuboidShape(6.5, 6.5, 9.5, 9.5, 9.5, 16.0)
        val NORTH_SHAPE: VoxelShape = createCuboidShape(6.5, 6.5, 6.5, 9.5, 9.5, 0.0)
        val EAST_SHAPE: VoxelShape = createCuboidShape(9.5, 6.5, 6.5, 16.0, 9.5, 9.5)
        val WEST_SHAPE: VoxelShape = createCuboidShape(0.0, 6.5, 6.5, 6.5, 9.5, 9.5)

        val CENTER_SHAPE: VoxelShape = createCuboidShape(6.5, 6.5, 6.5, 9.5, 9.5, 9.5)

        private fun getShape(direction: Direction): VoxelShape {
            var shape = VoxelShapes.empty()
            if (direction == Direction.NORTH) shape = NORTH_SHAPE
            if (direction == Direction.SOUTH) shape = SOUTH_SHAPE
            if (direction == Direction.EAST) shape = EAST_SHAPE
            if (direction == Direction.WEST) shape = WEST_SHAPE
            if (direction == Direction.UP) shape = UP_SHAPE
            if (direction == Direction.DOWN) shape = DOWN_SHAPE
            return shape
        }

    }
}
package me.steven.indrev.blocks.machine

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.storage.ChargePadBlockEntity
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.energyOf
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemPlacementContext
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import me.steven.indrev.utils.translatable
import net.minecraft.util.ActionResult
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.util.*
import java.util.stream.Stream

class ChargePadBlock(registry: MachineRegistry, settings: Settings, tier: Tier) :
    HorizontalFacingMachineBlock(registry, settings, tier, null, null) {

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape =
        when (state[HORIZONTAL_FACING]) {
            Direction.NORTH -> FACING_NORTH
            Direction.SOUTH -> FACING_SOUTH
            Direction.EAST -> FACING_EAST
            Direction.WEST -> FACING_WEST
            else -> FACING_NORTH
        }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult {
        val blockEntity = world.getBlockEntity(pos) as? ChargePadBlockEntity ?: return ActionResult.PASS
        val inventory = blockEntity.inventoryComponent?.inventory ?: return ActionResult.PASS
        val machineStack = inventory.getStack(0)
        if (!machineStack.isEmpty) {
            player?.inventory?.insertStack(machineStack)
            return ActionResult.SUCCESS
        }
        val handStack = player?.mainHandStack
        if (energyOf(player?.inventory, player?.inventory?.selectedSlot?: return ActionResult.PASS) != null) {
            inventory.setStack(0, handStack)
            player.setStackInHand(Hand.MAIN_HAND, ItemStack.EMPTY)
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        super.getPlacementState(ctx)
        return this.defaultState.with(HORIZONTAL_FACING, ctx?.playerFacing)
    }

    override fun onEntityCollision(state: BlockState?, world: World?, pos: BlockPos?, entity: Entity?) {
        if (entity is PlayerEntity || entity is ArmorStandEntity) {
            val blockEntity = world?.getBlockEntity(pos) as? ChargePadBlockEntity ?: return
            blockEntity.hasCollided = true
        }
    }

    override fun appendTooltip(stack: ItemStack?, view: BlockView?, tooltip: MutableList<Text>?, options: TooltipContext?) {
        super.appendTooltip(stack, view, tooltip, options)
        tooltip?.add(translatable("block.indrev.charge_pad_mk4.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC))
    }

    companion object {
        private val FACING_NORTH = Stream.of(
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 0.1, 15.0),
            createCuboidShape(1.25, 0.0, 1.25, 14.75, 0.3, 14.75),
            createCuboidShape(7.0, 0.0, 2.0, 9.0, 15.0, 4.0),
            createCuboidShape(8.0, 15.0, 2.0, 9.0, 15.5, 3.0),
            createCuboidShape(7.0, 15.0, 2.0, 8.0, 15.5, 3.0),
            createCuboidShape(7.0, 15.0, 3.0, 8.0, 15.2, 4.0),
            createCuboidShape(8.0, 15.0, 3.0, 9.0, 15.2, 4.0)
        ).reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)
        private val FACING_SOUTH: VoxelShape = Stream.of(
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 0.1, 15.0),
            createCuboidShape(1.25, 0.0, 1.25, 14.75, 0.3, 14.75),
            createCuboidShape(7.0, 0.0, 12.0, 9.0, 15.0, 14.0),
            createCuboidShape(7.0, 15.0, 13.0, 8.0, 15.5, 14.0),
            createCuboidShape(8.0, 15.0, 13.0, 9.0, 15.5, 14.0),
            createCuboidShape(8.0, 15.0, 12.0, 9.0, 15.2, 13.0),
            createCuboidShape(7.0, 15.0, 12.0, 8.0, 15.2, 13.0)
        ).reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)
        private val FACING_WEST = Stream.of(
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 0.1, 15.0),
            createCuboidShape(1.25, 0.0, 1.25, 14.75, 0.3, 14.75),
            createCuboidShape(2.0, 0.0, 7.0, 4.0, 15.0, 9.0),
            createCuboidShape(2.0, 15.0, 7.0, 3.0, 15.5, 8.0),
            createCuboidShape(2.0, 15.0, 8.0, 3.0, 15.5, 9.0),
            createCuboidShape(3.0, 15.0, 8.0, 4.0, 15.2, 9.0),
            createCuboidShape(3.0, 15.0, 7.0, 4.0, 15.2, 8.0)
        ).reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)
        private val FACING_EAST = Stream.of(
            createCuboidShape(1.0, 0.0, 1.0, 15.0, 0.1, 15.0),
            createCuboidShape(1.25, 0.0, 1.25, 14.75, 0.3, 14.75),
            createCuboidShape(12.0, 0.0, 7.0, 14.0, 15.0, 9.0),
            createCuboidShape(13.0, 15.0, 8.0, 14.0, 15.5, 9.0),
            createCuboidShape(13.0, 15.0, 7.0, 14.0, 15.5, 8.0),
            createCuboidShape(12.0, 15.0, 7.0, 13.0, 15.2, 8.0),
            createCuboidShape(12.0, 15.0, 8.0, 13.0, 15.2, 9.0)
        ).reduce { v1: VoxelShape?, v2: VoxelShape? -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR) }.orElse(null)
    }
}
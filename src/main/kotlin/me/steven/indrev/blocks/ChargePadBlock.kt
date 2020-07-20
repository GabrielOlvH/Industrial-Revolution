package me.steven.indrev.blocks

import com.google.common.collect.Iterables
import me.steven.indrev.blockentities.battery.ChargePadBlockEntity
import me.steven.indrev.utils.Tier
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
import net.minecraft.text.TranslatableText
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
import team.reborn.energy.Energy
import java.util.*
import java.util.stream.Stream

class ChargePadBlock(settings: Settings, tier: Tier) : FacingMachineBlock(settings, tier, null, { ChargePadBlockEntity(tier) }) {

    override fun getOutlineShape(state: BlockState, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape =
        when (state[HORIZONTAL_FACING]) {
            Direction.NORTH -> FACING_NORTH
            Direction.SOUTH -> FACING_SOUTH
            Direction.EAST -> FACING_EAST
            Direction.WEST -> FACING_WEST
            else -> FACING_NORTH
        }

    override fun onUse(state: BlockState?, world: World, pos: BlockPos?, player: PlayerEntity?, hand: Hand?, hit: BlockHitResult?): ActionResult? {
        val blockEntity = world.getBlockEntity(pos) as? ChargePadBlockEntity ?: return ActionResult.PASS
        val inventory = blockEntity.inventoryController?.inventory ?: return ActionResult.PASS
        val machineStack = inventory.getStack(0)
        if (!machineStack.isEmpty) {
            player?.inventory?.insertStack(machineStack)
            return ActionResult.SUCCESS
        }
        val handStack = player?.mainHandStack
        if (Energy.valid(handStack)) {
            val remaining = inventory.addStack(handStack)
            player?.setStackInHand(Hand.MAIN_HAND, remaining)
            return ActionResult.SUCCESS
        }
        return ActionResult.PASS
    }

    override fun getPlacementState(ctx: ItemPlacementContext?): BlockState? {
        super.getPlacementState(ctx)
        return this.defaultState.with(HORIZONTAL_FACING, ctx?.playerFacing)
    }

    override fun onEntityCollision(state: BlockState?, world: World?, pos: BlockPos?, entity: Entity?) {
        val blockEntity = world?.getBlockEntity(pos) as? ChargePadBlockEntity ?: return
        val items = when (entity) {
            is PlayerEntity ->
                Iterables.concat(entity.inventory.armor, mutableListOf(entity.mainHandStack, entity.offHandStack))
            is ArmorStandEntity -> entity.itemsEquipped
            else -> return
        }.filter { stack -> Energy.valid(stack) }.map { stack -> Energy.of(stack) }
        val sum = items.sumByDouble { it.maxInput.coerceAtLeast(it.energy) }
        val amount = sum / items.size.toDouble()
        val chargePadHandler = Energy.of(blockEntity)
        items.forEach { handler ->
            chargePadHandler.into(handler).move(amount)
        }
    }

    override fun buildTooltip(stack: ItemStack?, view: BlockView?, tooltip: MutableList<Text>?, options: TooltipContext?) {
        super.buildTooltip(stack, view, tooltip, options)
        tooltip?.add(TranslatableText("block.indrev.charge_pad_mk4.tooltip").formatted(Formatting.BLUE, Formatting.ITALIC))
    }

    @Environment(EnvType.CLIENT)
    override fun randomDisplayTick(state: BlockState?, world: World, pos: BlockPos, random: Random?) {
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
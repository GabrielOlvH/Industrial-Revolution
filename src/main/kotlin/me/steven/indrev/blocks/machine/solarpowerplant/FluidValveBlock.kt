package me.steven.indrev.blocks.machine.solarpowerplant

import alexiil.mc.lib.attributes.AttributeList
import alexiil.mc.lib.attributes.AttributeProvider
import alexiil.mc.lib.attributes.fluid.FluidAttributes
import alexiil.mc.lib.attributes.fluid.impl.EmptyGroupedFluidInv
import me.steven.indrev.blockentities.generators.SteamTurbineBlockEntity
import me.steven.indrev.blockentities.solarpowerplant.BoilerBlockEntity
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.FacingBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class FluidValveBlock(settings: Settings) : FacingBlock(settings), AttributeProvider {

    init {
        this.defaultState = stateManager.defaultState.with(FACING, Direction.NORTH)
    }

    override fun addAllAttributes(world: World, pos: BlockPos, state: BlockState, to: AttributeList<*>) {
        if (to.attribute != FluidAttributes.INSERTABLE && to.attribute != FluidAttributes.GROUPED_INV) {
            to.offer(EmptyGroupedFluidInv.INSTANCE)
        } else if (SteamTurbineBlockEntity.FLUID_VALVES_MAPPER.containsKey(pos.asLong())) {
            val turbinePos = BlockPos.fromLong(SteamTurbineBlockEntity.FLUID_VALVES_MAPPER[pos.asLong()])
            val blockEntity = world.getBlockEntity(turbinePos) as? SteamTurbineBlockEntity
            to.offer(blockEntity?.fluidComponent ?: EmptyGroupedFluidInv.INSTANCE)
        } else if (BoilerBlockEntity.FLUID_VALVES_MAPPER.containsKey(pos.asLong())) {
            val boilerPos = BlockPos.fromLong(BoilerBlockEntity.FLUID_VALVES_MAPPER[pos.asLong()])
            val blockEntity = world.getBlockEntity(boilerPos) as? BoilerBlockEntity
            to.offer(blockEntity?.fluidComponent ?: EmptyGroupedFluidInv.INSTANCE)
        }
    }

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>?) {
        builder?.add(FACING)
    }

    override fun rotate(state: BlockState, rotation: BlockRotation): BlockState? {
        return state.with(FACING, rotation.rotate(state[FACING]))
    }

    override fun mirror(state: BlockState, mirror: BlockMirror): BlockState? {
        return state.rotate(mirror.getRotation(state[FACING]))
    }

    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        return defaultState.with(FACING, ctx.playerLookDirection.opposite)
    }
}
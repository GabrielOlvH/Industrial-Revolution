package me.steven.indrev.networks.factory

import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable
import alexiil.mc.lib.attributes.fluid.impl.EmptyGroupedFluidInv
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable
import alexiil.mc.lib.attributes.item.impl.EmptyItemExtractable
import alexiil.mc.lib.attributes.item.impl.RejectingItemInsertable
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.blocks.machine.pipes.ItemPipeBlock
import me.steven.indrev.networks.NetworkState
import me.steven.indrev.networks.energy.EnergyNetwork
import me.steven.indrev.networks.fluid.FluidNetwork
import me.steven.indrev.networks.item.ItemNetwork
import me.steven.indrev.utils.*
import net.minecraft.block.BlockState
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

val ENERGY_NET_FACTORY: NetworkFactory<EnergyNetwork> = object : NetworkFactory<EnergyNetwork> {
    override fun process(
        network: EnergyNetwork,
        state: NetworkState<EnergyNetwork>,
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean {
        if (blockState().block is CableBlock) {
            network.appendPipe(blockState().block, pos.toImmutable())
            return true
        } else {
            val energyOf = energyOf(world, pos, direction)
            if (energyOf != null) {
                network.appendContainer(pos, direction.opposite)
                if (energyOf.supportsInsertion()) network.insertables.add(pos)
            }
        }
        return false
    }
}

val FLUID_NET_FACTORY: NetworkFactory<FluidNetwork> = object : NetworkFactory<FluidNetwork> {
    override fun process(
        network: FluidNetwork,
        state: NetworkState<FluidNetwork>,
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean {
        if (blockState().block is FluidPipeBlock) {
            network.appendPipe(blockState().block, pos.toImmutable())
            state.onSet(pos, network)
            return true
        } else if (
            fluidInsertableOf(world, pos, direction) != RejectingFluidInsertable.NULL
            || fluidExtractableOf(world, pos, direction) != EmptyFluidExtractable.NULL
            || groupedFluidInv(world, pos, direction) != EmptyGroupedFluidInv.INSTANCE
        ) {
            network.appendContainer(pos, direction.opposite)
        }
        return false
    }
}

val ITEM_NET_FACTORY: NetworkFactory<ItemNetwork> = object : NetworkFactory<ItemNetwork> {
    override fun process(
        network: ItemNetwork,
        state: NetworkState<ItemNetwork>,
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean {
        if (blockState().block is ItemPipeBlock) {
            network.appendPipe(blockState().block, pos.toImmutable())
            state.onSet(pos, network)
            return true
        } else if (
            itemInsertableOf(world, pos, direction) != RejectingItemInsertable.NULL
            || itemExtractableOf(world, pos, direction) != EmptyItemExtractable.NULL
        ) {
            network.appendContainer(pos, direction.opposite)
        }
        return false
    }
}
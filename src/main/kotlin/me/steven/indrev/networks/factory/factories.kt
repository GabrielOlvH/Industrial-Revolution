package me.steven.indrev.networks.factory

import alexiil.mc.lib.attributes.fluid.impl.EmptyFluidExtractable
import alexiil.mc.lib.attributes.fluid.impl.EmptyGroupedFluidInv
import alexiil.mc.lib.attributes.fluid.impl.RejectingFluidInsertable
import alexiil.mc.lib.attributes.item.impl.EmptyItemExtractable
import alexiil.mc.lib.attributes.item.impl.RejectingItemInsertable
import me.steven.indrev.blocks.machine.pipes.CableBlock
import me.steven.indrev.blocks.machine.pipes.FluidPipeBlock
import me.steven.indrev.blocks.machine.pipes.ItemPipeBlock
import me.steven.indrev.networks.Network
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
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean {
        if (energyOf(world, pos, direction) != null) {
            network.appendContainer(pos, direction.opposite)
        } else if (blockState().block is CableBlock) {
            network.appendPipe(blockState().block, pos.toImmutable())
            return true
        }
        return false
    }
}

val FLUID_NET_FACTORY: NetworkFactory<FluidNetwork> = object : NetworkFactory<FluidNetwork> {
    override fun process(
        network: FluidNetwork,
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean {
        if (
            fluidInsertableOf(world, pos, direction) != RejectingFluidInsertable.NULL
            || fluidExtractableOf(world, pos, direction) != EmptyFluidExtractable.NULL
            || groupedFluidInv(world, pos, direction) != EmptyGroupedFluidInv.INSTANCE
        ) {
            network.appendContainer(pos, direction.opposite)
        } else if (blockState().block is FluidPipeBlock) {
            network.appendPipe(blockState().block, pos.toImmutable())
            Network.Type.FLUID.getNetworkState(world)?.onSet(pos, network)
            return true
        }
        return false
    }
}

val ITEM_NET_FACTORY: NetworkFactory<ItemNetwork> = object : NetworkFactory<ItemNetwork> {
    override fun process(
        network: ItemNetwork,
        world: ServerWorld,
        pos: BlockPos,
        direction: Direction,
        blockState: () -> BlockState
    ): Boolean {
        if (
            itemInsertableOf(world, pos, direction) != RejectingItemInsertable.NULL
            || itemExtractableOf(world, pos, direction) != EmptyItemExtractable.NULL
        ) {
            network.appendContainer(pos, direction.opposite)
        } else if (blockState().block is ItemPipeBlock) {
            network.appendPipe(blockState().block, pos.toImmutable())
            Network.Type.ITEM.getNetworkState(world)?.onSet(pos, network)
            return true
        }
        return false
    }
}
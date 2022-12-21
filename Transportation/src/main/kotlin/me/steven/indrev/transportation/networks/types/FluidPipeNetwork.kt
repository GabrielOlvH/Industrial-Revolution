package me.steven.indrev.transportation.networks.types

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World

class FluidPipeNetwork(world: ServerWorld) : StoragePipeNetwork<FluidVariant>(world) {
    override val minimumTransferable: Long = 81L
    override val maximumTransferable: Long = 81_000L

    override fun find(world: World, pos: BlockPos, direction: Direction): Storage<FluidVariant>? {
        return FluidStorage.SIDED.find(world, pos, direction)
    }
}
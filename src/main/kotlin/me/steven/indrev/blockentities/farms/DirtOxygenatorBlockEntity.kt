package me.steven.indrev.blockentities.farms

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class DirtOxygenatorBlockEntity(pos: BlockPos, state: BlockState) : MachineBlockEntity<MachineConfig>(Tier.MK1, MachineRegistry.DIRT_OXYGENATOR_REGISTRY, pos, state) {

    private var streak = 0
    private var ticksUntilReset = 40
    val fluidInv = FluidStorage()


    override fun machineTick() {
        val target = pos.offset(cachedState[FacingMachineBlock.FACING]).up()
        val targetState = world!!.getBlockState(target)
        val block = targetState.block
        if (block !is Fertilizable || !targetState.canPlaceAt(world, target)) return
        if (fluidInv.amount <= 0) {
            ticksUntilReset--
            if (ticksUntilReset <= 0)
                streak = 0
            return
        } else {
            streak++
            ticksUntilReset = 40
        }
        val chance = (streak / 300.0).coerceIn(0.0, 1.0) * 0.6
        if (world!!.random.nextDouble() < chance) {
            if (
                block.isFertilizable(world, pos, targetState, false)
                && block.canGrow(world, world!!.random, target, targetState)
            ) {
                block.grow(world as ServerWorld, world!!.random, target, targetState)
            }
        }

        fluidInv.variant = FluidVariant.blank()
        fluidInv.amount = 0
    }

    inner class FluidStorage : SingleVariantStorage<FluidVariant>() {

        override fun canExtract(variant: FluidVariant): Boolean = false

        override fun canInsert(variant: FluidVariant): Boolean = variant.isOf(IRFluidRegistry.OXYGEN_STILL)

        override fun getCapacity(variant: FluidVariant): Long = bucket

        override fun getBlankVariant(): FluidVariant = FluidVariant.blank()
    }
}

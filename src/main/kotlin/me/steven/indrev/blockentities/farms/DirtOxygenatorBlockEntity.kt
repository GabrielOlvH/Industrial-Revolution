package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidInsertable
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.config.MachineConfig
import me.steven.indrev.registry.IRFluidRegistry
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.minus
import net.minecraft.block.BlockState
import net.minecraft.block.Fertilizable
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

class DirtOxygenatorBlockEntity(pos: BlockPos, state: BlockState) : MachineBlockEntity<MachineConfig>(Tier.MK1, MachineRegistry.DIRT_OXYGENATOR_REGISTRY, pos, state), FluidInsertable {

    private var inserted: FluidAmount = FluidAmount.ZERO
    private var streak = 0
    private var ticksUntilReset = 40

    override fun machineTick() {
        val target = pos.offset(cachedState[FacingMachineBlock.FACING]).up()
        val targetState = world!!.getBlockState(target)
        val block = targetState.block
        if (block !is Fertilizable || !targetState.canPlaceAt(world, target)) return
        if (inserted.isZero) {
            ticksUntilReset--
            if (ticksUntilReset <= 0)
                streak = 0
            return
        }
        val attempts = 1 + world!!.random.nextInt(2)
        val chance = (streak / 300.0).coerceIn(0.0, 0.7)
        repeat(attempts) {
            if (world!!.random.nextDouble() < chance) {
                if (
                    block.isFertilizable(world, pos, targetState, false)
                    && block.canGrow(world, world!!.random, target, targetState)
                ) {
                    block.grow(world as ServerWorld, world!!.random, target, targetState)
                }
            }
        }

        inserted = FluidAmount.ZERO
    }

    override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation): FluidVolume {
        if (fluid.fluidKey.rawFluid != IRFluidRegistry.OXYGEN_STILL) return fluid
        val inserted = fluid.amount().coerceAtMost(FluidAmount.of(1, 100))
        if (simulation.isAction) {
            this.inserted = inserted
            streak++
            ticksUntilReset = 40
        }
        val rem = fluid.amount() - inserted
        return if (rem > FluidAmount.ZERO) fluid.fluidKey.withAmount(rem) else FluidKeys.EMPTY.withAmount(FluidAmount.ZERO)
    }
}

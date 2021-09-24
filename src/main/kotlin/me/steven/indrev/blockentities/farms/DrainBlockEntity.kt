package me.steven.indrev.blockentities.farms

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.contains
import me.steven.indrev.utils.drainFluid
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction

class DrainBlockEntity(tier: Tier, pos: BlockPos, state: BlockState) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.DRAIN_REGISTRY, pos, state) {

    init {
        this.fluidComponent = FluidComponent({ this }, bucket)
    }

    override val maxInput: Long = config.maxInput

    override fun machineTick() {
        val world = world ?: return
        val fluidComponent = fluidComponent ?: return
        if (world.time % 20 != 0L || !fluidComponent[0].isEmpty) return

        val fluidState = world.getFluidState(pos.up())
        if (fluidState?.isEmpty == false && canUse(config.energyCost)) {
            val range = getWorkingArea()
            // DOWN is intentionally excluded
            val directions = arrayOf(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
            val mutablePos = pos.mutableCopy()
            val fluid = getStill(fluidState.fluid)
            val bfs = mutableListOf(pos.up())
            var i = 0
            while (i < bfs.size) {
                val current = bfs[i++]
                for (dir in directions) {
                    mutablePos.set(current, dir)
                    if (mutablePos !in bfs && mutablePos in range && getStill(world.getFluidState(mutablePos).fluid) === fluid) {
                        bfs.add(mutablePos.toImmutable())
                    }
                }
            }
            bfs.sortByDescending { it.y }

            for (pos in bfs) {
                val blockState = world.getBlockState(pos)
                val block = blockState?.block
                if (block is FluidDrainable && block is FluidBlock) {
                    val drained = block.drainFluid(world, pos, blockState)
                    if (drained != Fluids.EMPTY) {
                        fluidComponent[0].insert(FluidVariant.of(drained), bucket, true)
                        use(config.energyCost)
                        return
                    }
                }
            }
        }
    }

    private fun getStill(fluid: Fluid): Fluid = if (fluid is FlowableFluid) fluid.still else fluid

    fun getWorkingArea(): Box = Box(pos.up()).expand(8.0, 0.0, 8.0).stretch(0.0, 4.0, 0.0)

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        Direction.values().forEach { dir -> configuration[dir] = TransferMode.OUTPUT }
    }

    override fun isFixed(type: ConfigurationType): Boolean = true
}
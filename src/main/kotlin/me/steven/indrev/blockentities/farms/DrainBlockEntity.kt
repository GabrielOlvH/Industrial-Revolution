package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.TransferMode
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class DrainBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.DRAIN_REGISTRY) {

    init {
        this.fluidComponent = FluidComponent({ this }, FluidAmount.BUCKET)
    }

    override fun machineTick() {
        if ((world?.time ?: return) % 20 != 0L || !fluidComponent!!.tanks[0].volume.isEmpty) return
        val fluidComponent = fluidComponent ?: return
        val hasFluid = world?.getFluidState(pos.up())?.isEmpty == false
        val range = getWorkingArea()
        if (hasFluid && Energy.of(this).simulate().use(config.energyCost)) {
            val mutablePos = pos.mutableCopy()
            var currentChunk = world!!.getChunk(pos)
            for (x in range.minX.toInt()..range.maxX.toInt())
                for (y in range.minY.toInt()..range.maxY.toInt()) {
                    for (z in range.minZ.toInt()..range.maxZ.toInt()) {
                        mutablePos.set(x, y, z)
                        if (currentChunk.pos.x != x shr 4 && currentChunk.pos.z != z shr 4) {
                            currentChunk = world!!.getChunk(mutablePos)
                        }
                        val blockState = currentChunk.getBlockState(mutablePos)
                        val block = blockState?.block
                        if (block is FluidDrainable && block is FluidBlock) {
                            val drained = block.tryDrainFluid(world, mutablePos, blockState)
                            if (drained != Fluids.EMPTY) {
                                val toInsert = FluidKeys.get(drained).withAmount(FluidAmount.BUCKET)
                                currentChunk.setBlockState(mutablePos, Blocks.AIR.defaultState, false)
                                fluidComponent.insertable.insert(toInsert)
                                Energy.of(this).use(2.0)
                                break
                            }
                        }
                    }
                }
        }
    }

    fun getWorkingArea(): Box = Box(pos.up()).expand(8.0, 0.0, 8.0).stretch(0.0, 4.0, 0.0)

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        Direction.values().forEach { dir -> configuration[dir] = TransferMode.OUTPUT }
    }

    override fun isFixed(type: ConfigurationType): Boolean = true
}
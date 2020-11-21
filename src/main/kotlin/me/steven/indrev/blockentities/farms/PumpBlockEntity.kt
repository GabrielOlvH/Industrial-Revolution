package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.Fluids
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class PumpBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.PUMP_REGISTRY) {

    init {
        this.fluidComponent = FluidComponent({ this }, FluidAmount.BUCKET)
    }

    var currentLevel = 0
    var movingTicks = 0.0

    var ticks = 0

    override fun machineTick() {
        ticks++
        if (ticks % 20 != 0 || !fluidComponent!!.tanks[0].volume.isEmpty) return
        val lookLevel = pos.offset(Direction.DOWN, -currentLevel)
        val fluidComponent = fluidComponent ?: return
        if (world?.getFluidState(lookLevel)?.isEmpty == false) {
            if (world!!.isAir(lookLevel.down()))
                currentLevel++
            return
        }
        if (movingTicks < currentLevel) {
            movingTicks = (movingTicks + 1).coerceAtMost(currentLevel.toDouble())
            return
        }
        val range = getWorkingArea()
        val y = lookLevel.y
        if (Energy.of(this).simulate().use(config.energyCost)) {
            val mutablePos = lookLevel.mutableCopy()
            var currentChunk = world!!.getChunk(lookLevel)
            for (x in range.minX.toInt()..range.maxX.toInt()) {
                for (z in range.minZ.toInt()..range.maxZ.toInt()) {
                    mutablePos.set(x, y, z)
                    if (currentChunk.pos.x != x shr 4 && currentChunk.pos.z != z shr 4)
                        currentChunk = world!!.getChunk(mutablePos)
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

    fun getWorkingArea(): Box = Box(pos.up()).expand(8.0, 0.0, 8.0).stretch(0.0, 1.0, 0.0)

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0
}
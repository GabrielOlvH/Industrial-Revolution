package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide

class PumpBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.PUMP_REGISTRY), BlockEntityClientSerializable {

    init {
        this.fluidComponent = FluidComponent({ this }, FluidAmount.BUCKET)
    }

    var currentLevel = 1
    var movingTicks = 0.0

    var ticks = 0

    override fun machineTick() {
        ticks++
        val lookLevel = pos.offset(Direction.DOWN, currentLevel)
        if (movingTicks < currentLevel) {
            movingTicks = (movingTicks + 0.01).coerceAtMost(currentLevel.toDouble())
        } else if (world?.getFluidState(lookLevel)?.isEmpty == true && world!!.isAir(lookLevel.down())) {
            currentLevel++
        } else if (ticks % 20 == 0) {
            val fluidComponent = fluidComponent ?: return
            val range = getWorkingArea(lookLevel.down())
            val y = lookLevel.down().y
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
                            val toInsert = FluidKeys.get(blockState.fluidState.fluid).withAmount(FluidAmount.BUCKET)
                            if (fluidComponent.insertable.attemptInsertion(toInsert, Simulation.SIMULATE).isEmpty) {
                                block.tryDrainFluid(world, mutablePos, blockState)
                                fluidComponent.insertable.insert(toInsert)
                                Energy.of(this).use(2.0)
                                return
                            }
                        }
                    }
                }
            }
            return
        }
        sync()
    }

    fun getWorkingArea(center: BlockPos): Box = Box(center).expand(8.0, 0.0, 8.0).stretch(0.0, 1.0, 0.0)

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun fromClientTag(tag: CompoundTag?) {
        movingTicks = tag?.getDouble("MovingTicks") ?: movingTicks
        currentLevel = tag?.getInt("CurrentLevel") ?: currentLevel
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("MovingTicks", movingTicks)
        tag?.putInt("CurrentLevel", currentLevel)
        return super.toClientTag(tag)
    }
}
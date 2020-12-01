package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.TransferMode
import me.steven.indrev.utils.map
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.FluidDrainable
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import team.reborn.energy.Energy
import team.reborn.energy.EnergySide
import kotlin.math.floor
import kotlin.math.roundToInt

class PumpBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.PUMP_REGISTRY), BlockEntityClientSerializable {

    init {
        this.fluidComponent = FluidComponent({ this }, FluidAmount.BUCKET)
    }

    var movingTicks = 0.0
    var isDescending = false
    var lastYPos = 0

    override fun machineTick() {
        val currentLevel = floor(movingTicks).toInt().coerceAtLeast(1)
        val lookLevel = pos.offset(Direction.DOWN, currentLevel)
        if (!isDescending && ticks % 20 == 0 && Energy.of(this).simulate().use(config.energyCost)) {
            if (world?.getFluidState(lookLevel)?.isEmpty == true) {
                lastYPos = lookLevel.y
                isDescending = true
                return
            }
            movingTicks = movingTicks.roundToInt().toDouble()
            val fluidComponent = fluidComponent ?: return
            val areaBox = getWorkingArea(lookLevel)
            val areaIterator = areaBox.map(::BlockPos).sortedWith(compareBy { it.getSquaredDistance(pos) }).iterator()
            while (areaIterator.hasNext()) {
                val fluidPos = areaIterator.next()
                val fluidState = world?.getFluidState(fluidPos)
                val fluid = fluidState?.fluid
                val block = fluidState?.blockState?.block
                if (block is FluidDrainable && !fluidState.isEmpty && fluidState.isStill) {
                    val toInsert = FluidKeys.get(fluid).withAmount(FluidAmount.BUCKET)
                    if (fluidComponent.insertable.attemptInsertion(toInsert, Simulation.SIMULATE).isEmpty) {
                        block.tryDrainFluid(world, fluidPos, fluidState.blockState)
                        fluidComponent.insertable.insert(toInsert)
                        Energy.of(this).use(2.0)
                    }
                    return
                }
            }
            lastYPos = lookLevel.y
            isDescending = true
            return
        }
        else if (world?.getFluidState(lookLevel)?.isEmpty == false && lookLevel.y < lastYPos)
            isDescending = false
        else if (world?.isAir(lookLevel) == true || world?.getFluidState(lookLevel)?.isEmpty == false)
            movingTicks += 0.01
        sync()
    }

    override fun applyDefault(
        state: BlockState,
        type: ConfigurationType,
        configuration: MutableMap<Direction, TransferMode>
    ) {
        assert(type == ConfigurationType.FLUID)
        val facing = state[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
        configuration[facing] = TransferMode.OUTPUT
    }

    override fun isFixed(type: ConfigurationType): Boolean = true

    private fun getWorkingArea(center: BlockPos): Box = Box(center).expand(9.0, 0.0, 9.0)

    override fun getMaxInput(side: EnergySide?): Double = config.maxInput

    override fun getMaxOutput(side: EnergySide?): Double = 0.0

    override fun fromClientTag(tag: CompoundTag?) {
        movingTicks = tag?.getDouble("MovingTicks") ?: movingTicks
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("MovingTicks", movingTicks)
        return super.toClientTag(tag)
    }
}
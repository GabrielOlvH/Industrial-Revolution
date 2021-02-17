package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.contains
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import kotlin.math.floor
import kotlin.math.roundToInt

class PumpBlockEntity(tier: Tier) : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.PUMP_REGISTRY), BlockEntityClientSerializable {

    init {
        this.fluidComponent = FluidComponent({ this }, FluidAmount.BUCKET)
    }

    override val maxInput: Double = config.maxInput
    override val maxOutput: Double = 0.0

    var movingTicks = 0.0
    var isDescending = false
    var lastYPos = -1

    override fun machineTick() {
        val fluidComponent = fluidComponent ?: return
        val currentLevel = floor(movingTicks).toInt()
        val lookLevel = pos.offset(Direction.DOWN, currentLevel)
        val currentFluid = world?.getFluidState(lookLevel)
        if (!isDescending && ticks % config.processSpeed.toInt() == 0 && canUse(config.energyCost) && fluidComponent[0].isEmpty) {
            if (currentFluid?.isEmpty == false && canUse(config.energyCost)) {
                val range = getWorkingArea(lookLevel)
                val directions = arrayOf(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
                val mutablePos = pos.mutableCopy()
                val fluid = getStill(currentFluid.fluid)
                val bfs = mutableListOf(lookLevel)
                var i = 0
                while (i < bfs.size) {
                    val current = bfs[i++]
                    for (dir in directions) {
                        mutablePos.set(current, dir)
                        if (mutablePos !in bfs && mutablePos in range && getStill(world!!.getFluidState(mutablePos).fluid) === fluid) {
                            bfs.add(mutablePos.toImmutable())
                        }
                    }
                }

                for (pos in bfs) {
                    val blockState = world!!.getBlockState(pos)
                    val block = blockState?.block
                    if (block is FluidDrainable && block is FluidBlock) {
                        val drained = block.tryDrainFluid(world, pos, blockState)
                        if (drained != Fluids.EMPTY) {
                            val toInsert = FluidKeys.get(drained).withAmount(FluidAmount.BUCKET)
                            fluidComponent.insertable.insert(toInsert)
                            use(config.energyCost)
                            return
                        }
                    }
                }
            }
            lastYPos = lookLevel.y
            isDescending = true
        }
        else if (currentFluid?.isEmpty == false && lookLevel.y < lastYPos) {
            isDescending = false
            movingTicks = movingTicks.roundToInt().toDouble()
        } else if (use(2.0) && (lookLevel == pos || (world?.isAir(lookLevel) == true && currentFluid?.isEmpty != false))) {
            movingTicks += 0.01
            sync()
        }
    }

    private fun getStill(fluid: Fluid): Fluid = if (fluid is FlowableFluid) fluid.still else fluid

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

    private fun getWorkingArea(center: BlockPos): Box = Box(center).expand(7.0, 0.0, 7.0)

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("MovingTicks", movingTicks)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        movingTicks = tag?.getDouble("MovingTicks") ?: movingTicks
        super.fromTag(state, tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        movingTicks = tag?.getDouble("MovingTicks") ?: movingTicks
        super.fromClientTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putDouble("MovingTicks", movingTicks)
        return super.toClientTag(tag)
    }
}
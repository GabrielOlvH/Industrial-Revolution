package me.steven.indrev.blockentities.farms

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.*
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.config.IRConfig
import me.steven.indrev.inventories.inventory
import me.steven.indrev.items.upgrade.Enhancer
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.drainFluid
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import kotlin.math.floor
import kotlin.math.roundToInt

class PumpBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.PUMP_REGISTRY, pos, state) {

    init {
        this.fluidComponent = FluidComponent({ this }, bucket)
        this.enhancerComponent = object : EnhancerComponent(intArrayOf(0, 1, 2, 3), Enhancer.DEFAULT, this::getMaxCount) {
            override fun isLocked(slot: Int, tier: Tier): Boolean = false
        }
        this.inventoryComponent = inventory(this) {
        }

        trackObject(TANK_ID, fluidComponent!![0])
        trackDouble(SPEED_ID) { config.processSpeed  - enhancerComponent!!.getCount(Enhancer.SPEED) * 10 }
    }

    override val syncToWorld: Boolean = true

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    var pipePosition = 0.0
    private var isSearchingDown = false

    private var currentTarget: BlockPos = pos

    private val scanned = LongOpenHashSet()
    private var queue = LongArrayFIFOQueue()
    private var cooldown = 20.0

    override fun machineTick() {
        if (pipePosition == 0.0) {
            isSearchingDown = true
        }

        if (!isSearchingDown && currentTarget == pos) {
            searchFluid()
        }

        if (queue.isEmpty) {
            isSearchingDown = true
        }

        if (currentTarget != pos) {
            cooldown--
            if (cooldown <= 0) {
                drainTargetFluid()
            }
        }

        if (isSearchingDown) {
            searchDown()
        }
    }

    private fun searchFluid() {
        val currentLevel = floor(pipePosition).toInt()
        val lookLevel = pos.offset(Direction.DOWN, currentLevel)
        val currentFluid = world!!.getFluidState(lookLevel)
        val targetFluid = world!!.getFluidState(currentTarget)

        if (currentFluid.isEmpty) {
            isSearchingDown = true
            return
        }

        if ((targetFluid.isEmpty || !targetFluid.isStill) && !currentFluid.isEmpty) {
            scanned.clear()
            val searching = BlockPos.Mutable()
            val neighbor = BlockPos.Mutable()
            while (!queue.isEmpty) {
                val packedPos = queue.dequeue()
                searching.set(BlockPos.unpackLongX(packedPos), BlockPos.unpackLongY(packedPos), BlockPos.unpackLongZ(packedPos))
                SEARCH_DIRECTIONS.forEach { dir ->
                    neighbor.set(searching.x + dir.offsetX, searching.y + dir.offsetY, searching.z + dir.offsetZ)
                    if (neighbor.getSquaredDistance(lookLevel) < IRConfig.machines.pumpMaxRange * IRConfig.machines.pumpMaxRange
                        && scanned.add(neighbor.asLong())
                        && getStill(world!!.getFluidState(neighbor).fluid) == getStill(currentFluid.fluid)) {
                        queue.enqueue(neighbor.asLong())
                    }
                }
                val fluidState = world!!.getFluidState(searching)
                if (fluidState.isStill && !fluidState.isEmpty && fluidState.fluid == getStill(currentFluid.fluid)) {
                    currentTarget = searching
                    break
                }
            }
        }
    }

    private fun getFluidTouchingPipe(): FluidState {
        val currentLevel = floor(pipePosition).toInt()
        val lookLevel = pos.offset(Direction.DOWN, currentLevel)
        return world!!.getFluidState(lookLevel)
    }

    private fun drainTargetFluid() {
        val fluidToPump = world!!.getFluidState(currentTarget)
        if (!fluidToPump.isEmpty && canUse(config.energyCost) && fluidComponent!![0].isEmpty) {
            val blockState = world!!.getBlockState(currentTarget)
            val block = blockState?.block
            if (block is FluidDrainable && block is FluidBlock) {
                val drained = block.drainFluid(world!!, currentTarget, blockState)
                if (drained != Fluids.EMPTY) {
                    fluidComponent!![0].insert(FluidVariant.of(drained), bucket, true)
                    use(getEnergyCost())
                    cooldown = config.processSpeed - enhancerComponent!!.getCount(Enhancer.SPEED) * 10
                }
                currentTarget = pos
            }
        }
    }

    override fun getEnergyCost(): Long {
        return config.energyCost * (enhancerComponent!!.getCount(Enhancer.SPEED) + 1)
    }

    private fun searchDown() {
        if (use(2)) {
            pipePosition += 0.01
            sync()
            val fluid = getFluidTouchingPipe()
            if (!fluid.isEmpty) {
                isSearchingDown = false
                pipePosition = pipePosition.roundToInt().toDouble()
                queue = LongArrayFIFOQueue()
                val currentLevel = floor(pipePosition).toInt()
                queue.enqueue(pos.offset(Direction.DOWN, currentLevel).asLong())
            }
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
    fun getMaxCount(enhancer: Enhancer): Int {
        return when (enhancer) {
            Enhancer.SPEED -> return 1
            Enhancer.BUFFER -> 4
            else -> 1
        }
    }


    override fun toTag(tag: NbtCompound) {
        tag.putDouble("MovingTicks", pipePosition)
        super.toTag(tag)
    }

    override fun fromTag(tag: NbtCompound) {
        pipePosition = tag.getDouble("MovingTicks")
        super.fromTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        pipePosition = tag.getDouble("MovingTicks")
    }

    override fun toClientTag(tag: NbtCompound) {
        tag.putDouble("MovingTicks", pipePosition)
    }

    companion object {
        const val TANK_ID = 2
        const val SPEED_ID = 3
        val SEARCH_DIRECTIONS = mutableListOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)
    }
}
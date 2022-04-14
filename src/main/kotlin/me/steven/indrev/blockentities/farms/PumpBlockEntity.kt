package me.steven.indrev.blockentities.farms

import kotlinx.coroutines.*
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.api.machines.TransferMode
import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.config.BasicMachineConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.drainFluid
import me.steven.indrev.utils.submitAndGet
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.block.FluidBlock
import net.minecraft.block.FluidDrainable
import net.minecraft.fluid.FlowableFluid
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.Fluids
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.MinecraftServer
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.math.floor
import kotlin.math.roundToInt

class PumpBlockEntity(tier: Tier, pos: BlockPos, state: BlockState)
    : MachineBlockEntity<BasicMachineConfig>(tier, MachineRegistry.PUMP_REGISTRY, pos, state) {

    init {
        this.fluidComponent = FluidComponent({this}, bucket)
    }

    override val syncToWorld: Boolean = true

    override val maxInput: Long = config.maxInput
    override val maxOutput: Long = 0

    var movingTicks = 0.0
    var isDescending = false
    var lastYPos = -1

    var currentTarget: BlockPos = pos

    var job: Job? = null
    var continuation: CancellableContinuation<Unit>? = null
    var count: Int = 0
    val scanned = mutableSetOf<BlockPos>()

    val directions = mutableListOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)

    override fun machineTick() {
        val world = world ?: return
        val fluidComponent = fluidComponent ?: return
        val currentLevel = floor(movingTicks).toInt()
        val lookLevel = pos.offset(Direction.DOWN, currentLevel)
        val currentFluid = world.getFluidState(lookLevel)

        val fluidState = world.getFluidState(currentTarget)

        if ((fluidState.isEmpty || !fluidState.isStill) && !currentFluid.isEmpty) {
            val server = (world as ServerWorld).server

            if (job?.isCancelled != false) {
                scanned.clear()
                job = GlobalScope.launch(DISPATCHER) {
                    val start = pos.offset(Direction.DOWN, floor(movingTicks).toInt())
                    val startFluid = world.getFluidState(start)
                    scan(start, getStill(startFluid.fluid), server, scanned)
                }
                return
            } else if (job?.isCompleted == true) {
                currentTarget = lookLevel
                job = null
            } else {
                count = 0
                continuation?.resume(Unit)
                continuation = null
            }
        }

        val fluidToPump = world.getFluidState(currentTarget)

        if (!isDescending && ticks % config.processSpeed.toInt() == 0 && canUse(config.energyCost)) {
            if (!fluidToPump.isEmpty && canUse(config.energyCost) && fluidComponent[0].isEmpty) {
                val blockState = world.getBlockState(currentTarget)
                val block = blockState?.block
                if (block is FluidDrainable && block is FluidBlock) {
                    val drained = block.drainFluid(world, currentTarget, blockState)
                    if (drained != Fluids.EMPTY) {
                        fluidComponent[0].insert(FluidVariant.of(drained), bucket, true)
                        use(config.energyCost)
                    }
                }
            }
        } else if (currentFluid?.isEmpty == false && lookLevel.y < lastYPos) {
            isDescending = false
            movingTicks = movingTicks.roundToInt().toDouble()
        } else if ((lookLevel == pos || (world.isAir(lookLevel) && currentFluid?.isEmpty != false)) && use(2)) {
            movingTicks += 0.01
            sync()
        }
    }

    private fun getStill(fluid: Fluid): Fluid = if (fluid is FlowableFluid) fluid.still else fluid

    private suspend fun scan(pos: BlockPos, fluid: Fluid, server: MinecraftServer, scanned: MutableSet<BlockPos>) {
        val world = world ?: return
        val centerBlock = this.pos.offset(Direction.DOWN, floor(movingTicks).toInt())

        count++
        if (count > 10) {
            suspendCancellableCoroutine { cont: CancellableContinuation<Unit> ->
                this.continuation = cont
            }
        }
        directions.shuffled(world.random).associate { dir ->
            val offset = pos.offset(dir)
            val fluidState = server.submitAndGet { world.getFluidState(offset) }
            offset to fluidState
        }.entries.sortedByDescending { if (it.value.isStill) 20 else it.value.level }.forEach { (offset, fluidState) ->
            if (offset != centerBlock && fluidState.fluid == fluid) {
                currentTarget = offset
                job?.cancel()
            } else if (scanned.add(offset) && getStill(fluidState.fluid) == fluid && !fluidState.isStill)
                scan(offset, fluid, server, scanned)
        }
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

    override fun toTag(tag: NbtCompound) {
        tag?.putDouble("MovingTicks", movingTicks)
        super.toTag(tag)
    }

    override fun fromTag(tag: NbtCompound) {
        movingTicks = tag.getDouble("MovingTicks")
        super.fromTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        movingTicks = tag.getDouble("MovingTicks")
    }

    override fun toClientTag(tag: NbtCompound) {
        tag.putDouble("MovingTicks", movingTicks)
    }

    companion object {
        val DISPATCHER = Executors.newSingleThreadExecutor {
            val t = Thread(it)
            t.name = "IR Pump Thread"
            t.isDaemon = true
            t
        }.asCoroutineDispatcher()
    }
}
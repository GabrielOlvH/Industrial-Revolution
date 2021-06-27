package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.GroupedFluidInv
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidKeys
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.IRSyncableBlockEntity
import me.steven.indrev.blocks.misc.TankBlock
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.plus
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.math.RoundingMode

class TankBlockEntity(pos: BlockPos, state: BlockState) : IRSyncableBlockEntity(IRBlockRegistry.TANK_BLOCK_ENTITY, pos, state) {
    val fluidComponent = FluidComponent({ this }, FluidAmount.ofWhole(8))

    companion object {
        fun tick(world: World, pos: BlockPos, state: BlockState, blockEntity: TankBlockEntity) {
            if (world.isClient) return
            if (blockEntity.isMarkedForUpdate) {
                blockEntity.markDirty()
                blockEntity.sync()
                blockEntity.isMarkedForUpdate = false
            }
            if (!state[TankBlock.DOWN]) return
            val down = world.getBlockEntity(pos.down()) as? TankBlockEntity ?: return
            val volume = FluidVolumeUtil.move(blockEntity.fluidComponent, down.fluidComponent)
            if (!volume.amount().isZero) {
                down.isMarkedForUpdate = true
                blockEntity.isMarkedForUpdate = true
            }
        }
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        fluidComponent.toTag(tag)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound?) {
        super.readNbt(tag)
        fluidComponent.fromTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound?) {
        fluidComponent.fromTag(tag)
    }

    override fun toClientTag(tag: NbtCompound): NbtCompound {
        fluidComponent.toTag(tag)
        return tag
    }

    override fun markDirty() {
        if (world != null) {
            world!!.markDirty(pos)
        }
    }

    class GroupedTankFluidInv : GroupedFluidInv {

        private val tanks = mutableListOf<FluidComponent>()

        var initialFluid = FluidKeys.EMPTY

        fun add(tank: TankBlockEntity): Boolean {
            val invFluid = tank.fluidComponent.getInvFluid(0)
            if (initialFluid.isEmpty && !invFluid.isEmpty) {
                initialFluid = invFluid.fluidKey
            } else if (!invFluid.isEmpty && initialFluid != invFluid.fluidKey) {
                IndustrialRevolution.LOGGER.warn("Found connected tanks with mismatching fluids @ ${tank.pos}")
                return false
            }
            tanks.add(tank.fluidComponent)
            return true
        }

        override fun getStoredFluids(): Set<FluidKey> = setOf(initialFluid)

        override fun getStatistics(filter: FluidFilter?): GroupedFluidInvView.FluidInvStatistic {
            return GroupedFluidInvView.FluidInvStatistic.emptyOf(filter)
        }

        override fun getAmount_F(filter: FluidFilter): FluidAmount {
            return if (!filter.matches(initialFluid)) FluidAmount.ZERO
            else
                tanks.map { it.getInvFluid(0).amount() }.reduce { first, second -> first + second }
        }

        override fun attemptInsertion(fluid: FluidVolume, simulation: Simulation): FluidVolume {
            if (!initialFluid.isEmpty && fluid.fluidKey != initialFluid) return fluid
            var fluid = fluid
            for (insertable in tanks) {
                fluid = insertable.attemptInsertion(fluid, simulation)
                if (fluid.isEmpty) {
                    return FluidVolumeUtil.EMPTY
                }
            }
            return fluid
        }

        override fun attemptExtraction(
            filter: FluidFilter,
            maxAmount: FluidAmount,
            simulation: Simulation
        ): FluidVolume {
            require(!maxAmount.isNegative) { "maxCount cannot be negative! (was $maxAmount)" }
            var extracted = FluidVolumeUtil.EMPTY
            if (maxAmount.isZero || (!initialFluid.isEmpty && !filter.matches(initialFluid))) {
                return extracted!!
            }
            val filter = initialFluid.exactFilter
            for (extractable in tanks) {
                if (extracted!!.isEmpty) {
                    extracted = extractable.attemptExtraction(filter, maxAmount, simulation)
                    if (extracted.isEmpty) {
                        continue
                    }
                    if (extracted.amount_F >= maxAmount) {
                        return extracted
                    }
                } else {
                    val newMaxAmount = maxAmount.roundedSub(extracted.amount_F, RoundingMode.UP)
                    val additional = extractable.attemptExtraction(filter, newMaxAmount, simulation)
                    if (additional.isEmpty) {
                        continue
                    }
                    extracted = FluidVolume.merge(extracted, additional)
                    checkNotNull(extracted) { "bad FluidExtractable " + extractable.javaClass.name }
                    if (extracted.amount_F >= maxAmount)  {
                        return extracted
                    }
                }
            }
            return extracted!!
        }

    }
}
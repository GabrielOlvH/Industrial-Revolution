package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.Simulation
import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.GroupedFluidInvView
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import alexiil.mc.lib.attributes.fluid.filter.FluidFilter
import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.SyncableBlockEntity
import me.steven.indrev.blocks.misc.TankBlock
import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.plus
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.math.RoundingMode

class TankBlockEntity(pos: BlockPos, state: BlockState) : SyncableBlockEntity(IRBlockRegistry.TANK_BLOCK_ENTITY, pos, state), BlockEntityClientSerializable {
    val fluidComponent = FluidComponent({ this }, bucket * 8)

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
            val volume = StorageUtil.move(blockEntity.fluidComponent, down.fluidComponent, { true }, Long.MAX_VALUE, null)
            if (volume > 0) {
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

    override fun <T> get(key: ComponentKey<T>): Any? {
        return when (key) {
            ComponentKey.FLUID -> fluidComponent
            else -> null
        }
    }

    class CombinedTankStorage : CombinedStorage<FluidVariant, FluidComponent>(mutableListOf<FluidComponent>()) {

        var initialFluid = FluidVariant.blank()

        fun add(tank: TankBlockEntity): Boolean {
            val invFluid = tank.fluidComponent[0]
            if (initialFluid.isBlank && !invFluid.isEmpty) {
                initialFluid = invFluid.variant
            } else if (!invFluid.isEmpty && initialFluid != invFluid.variant) {
                IndustrialRevolution.LOGGER.debug("Found connected tanks with mismatching fluids @ ${tank.pos}")
                return false
            }
            parts.add(tank.fluidComponent)
            return true
        }

        override fun insert(resource: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
            if (!initialFluid.isBlank && resource != initialFluid) return 0
            return super.insert(resource, maxAmount, transaction)
        }

        override fun extract(resource: FluidVariant?, maxAmount: Long, transaction: TransactionContext?): Long {
            if (!initialFluid.isBlank && resource != initialFluid) return 0
            return super.extract(resource, maxAmount, transaction)
        }
    }
}
package me.steven.indrev.blockentities.storage

import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.BaseBlockEntity
import me.steven.indrev.blockentities.Syncable
import me.steven.indrev.blocks.misc.TankBlock
import me.steven.indrev.components.FluidComponent
import me.steven.indrev.registry.IRBlockRegistry
import me.steven.indrev.utils.bucket
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TankBlockEntity(pos: BlockPos, state: BlockState) : BaseBlockEntity(IRBlockRegistry.TANK_BLOCK_ENTITY, pos, state), Syncable {
    val fluidComponent = object : FluidComponent({ this }, bucket * 8) {
        init {
            this.unsided = true
        }
    }

    var isMarkedForUpdate: Boolean = true

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

    override fun toTag(tag: NbtCompound) {
        fluidComponent.toTag(tag)
    }

    override fun fromTag(tag: NbtCompound) {
        fluidComponent.fromTag(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        fluidComponent.toTag(tag)
    }

    override fun fromClientTag(tag: NbtCompound) {
        fluidComponent.fromTag(tag)
    }

    override fun markDirty() {
        if (world != null) {
            world!!.markDirty(pos)
        }
    }

    override fun markForUpdate(condition: () -> Boolean) {
        isMarkedForUpdate = isMarkedForUpdate || condition()
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
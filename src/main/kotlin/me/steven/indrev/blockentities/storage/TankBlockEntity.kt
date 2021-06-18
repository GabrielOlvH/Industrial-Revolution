package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.blockentities.IRSyncableBlockEntity
import me.steven.indrev.blocks.misc.TankBlock
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class TankBlockEntity(pos: BlockPos, state: BlockState) : IRSyncableBlockEntity(IRBlockRegistry.TANK_BLOCK_ENTITY, pos, state) {
    val fluidComponent = FluidComponent(this, FluidAmount.ofWhole(8))

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
}
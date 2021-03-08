package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.fluid.FluidVolumeUtil
import alexiil.mc.lib.attributes.fluid.amount.FluidAmount
import me.steven.indrev.blockentities.IRSyncableBlockEntity
import me.steven.indrev.blocks.misc.TankBlock
import me.steven.indrev.components.fluid.FluidComponent
import me.steven.indrev.registry.IRBlockRegistry
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Tickable

class TankBlockEntity : IRSyncableBlockEntity(IRBlockRegistry.TANK_BLOCK_ENTITY), Tickable {
    val fluidComponent = FluidComponent({ this }, FluidAmount.ofWhole(8))

    override fun tick() {
        if (world?.isClient == true) return
        if (isMarkedForUpdate) {
            markDirty()
            sync()
            isMarkedForUpdate = false
        }
        if (!cachedState[TankBlock.DOWN]) return
        val down = world?.getBlockEntity(pos.down()) as? TankBlockEntity ?: return
        val volume = FluidVolumeUtil.move(fluidComponent, down.fluidComponent)
        if (!volume.amount().isZero) {
            down.isMarkedForUpdate = true
            isMarkedForUpdate = true
        }
    }

    override fun toTag(tag: CompoundTag): CompoundTag {
        fluidComponent.toTag(tag)
        return super.toTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        fluidComponent.fromTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        fluidComponent.fromTag(tag)
    }

    override fun toClientTag(tag: CompoundTag): CompoundTag {
        fluidComponent.toTag(tag)
        return tag
    }

    override fun markDirty() {
        if (world != null) {
            world!!.markDirty(pos, this)
        }
    }
}
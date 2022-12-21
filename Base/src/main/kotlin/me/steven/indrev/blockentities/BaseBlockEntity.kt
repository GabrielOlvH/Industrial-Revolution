package me.steven.indrev.blockentities

import com.google.common.base.Preconditions
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos

abstract class BaseBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BlockEntity(type, pos, state) {

    fun sync() {
        Preconditions.checkNotNull(world) // Maintain distinct failure case from below
        check(world is ServerWorld) { "Cannot call sync() on the logical client! Did you check world.isClient first?" }
        (world as ServerWorld).chunkManager.markForUpdate(getPos())
    }

    abstract fun toTag(nbt: NbtCompound)

    abstract fun fromTag(nbt: NbtCompound)

    open fun toClientTag(nbt: NbtCompound) {
    }

    open fun fromClientTag(nbt: NbtCompound) {
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        toClientTag(nbt)
        nbt.putBoolean("#c", true)
        return nbt
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        toTag(nbt)
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        if (nbt.contains("#c")) {
            fromClientTag(nbt)
        } else {
            fromTag(nbt)
        }
    }
}
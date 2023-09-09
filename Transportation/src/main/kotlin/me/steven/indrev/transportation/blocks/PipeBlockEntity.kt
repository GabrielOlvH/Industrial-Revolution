package me.steven.indrev.transportation.blocks

import com.google.common.base.Preconditions
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import me.steven.indrev.transportation.PIPE_BLOCK_ENTITY
import me.steven.indrev.transportation.networks.ConnectionType
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PipeBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(PIPE_BLOCK_ENTITY, pos, state), RenderAttachmentBlockEntity {
    val config = Int2IntOpenHashMap()

    init {
        config.defaultReturnValue(0)
    }

    inline fun forEachDirection(block: (Direction, ConnectionType) -> Unit) {
        config.forEach { (dirId, typeId) -> block(Direction.byId(dirId), ConnectionType.BY_ID[typeId]) }
    }

    fun set(dir: Direction, type: ConnectionType) {
        config[dir.id] = type.id
    }

    fun cycle(dir: Direction): ConnectionType {
        config[dir.id] = (config.get(dir.id) + 1) % 3
        val type = get(dir)
        if (type == ConnectionType.NONE)
            config.remove(dir.id)
        return type
    }

    fun get(dir: Direction): ConnectionType {
        return ConnectionType.BY_ID[config.get(dir.id)]
    }

    override fun readNbt(nbt: NbtCompound) {
        super.readNbt(nbt)
        val configNbt = nbt.getCompound("config")
        configNbt.keys.forEach { key ->
            val value = configNbt.getInt(key)
            config[key.toInt()] = value
        }

        if (nbt.getBoolean("#c")) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(world, pos, null, null, 0)
        }
    }

    override fun writeNbt(nbt: NbtCompound) {
        super.writeNbt(nbt)
        val configNbt = NbtCompound()
        config.forEach { (dirId, typeId) -> configNbt.putInt(dirId.toString(), typeId) }
        nbt.put("config", configNbt)
    }

    fun sync() {
        Preconditions.checkNotNull(world)
        check(world is ServerWorld) { "Cannot call sync() on the logical client! Did you check world.isClient first?" }
        (world as ServerWorld).chunkManager.markForUpdate(getPos())
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        writeNbt(nbt)
        nbt.putBoolean("#c", true)
        return nbt
    }

    override fun getRenderAttachmentData(): Int2IntOpenHashMap {
        return config
    }
}
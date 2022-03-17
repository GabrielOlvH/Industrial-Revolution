package me.steven.indrev.blockentities.cables

import com.google.common.base.Preconditions
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.networks.Network
import me.steven.indrev.registry.IRBlockRegistry
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtOps
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class BasePipeBlockEntity(val pipeType: Network.Type<*>, tier: Tier, pos: BlockPos, state: BlockState) :
    BlockEntity(when (tier) {
        Tier.MK1 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK1
        Tier.MK2 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK2
        Tier.MK3 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK3
        Tier.MK4 -> IRBlockRegistry.COVERABLE_BLOCK_ENTITY_TYPE_MK4
        Tier.CREATIVE -> error("no creative cable")
                            }, pos, state), RenderAttachmentBlockEntity {

    val connections = Object2ObjectOpenHashMap<Direction, BasePipeBlock.ConnectionType>()

    init {
        connections.defaultReturnValue(BasePipeBlock.ConnectionType.NONE)
    }

    var coverState: BlockState? = null

    override fun getRenderAttachmentData(): Any {
        return PipeRenderData(
            coverState,
            connections.filterValues { type -> type == BasePipeBlock.ConnectionType.CONNECTED }.keys.toTypedArray()
        )
    }

    override fun readNbt(tag: NbtCompound) {
        this.coverState = null
        if (tag.contains("coverState")) {
            BlockState.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("coverState")).result().ifPresent { pair ->
                this.coverState = pair.first
            }
        }
        val list = tag.getList("connections", 10)
        connections.clear()
        list?.forEach { t ->
            t as NbtCompound
            val dir = Direction.byId(t.getByte("d").toInt())
            val type = BasePipeBlock.ConnectionType.byId(t.getByte("c").toInt())
            connections[dir] = type
        }
        super.readNbt(tag)

        if (world != null && world!!.isClient) {
            MinecraftClient.getInstance().worldRenderer.updateBlock(world!!, pos, null, null, 0)
        }
    }

    override fun writeNbt(tag: NbtCompound) {
        if (this.coverState != null) {
            BlockState.CODEC.encode(this.coverState, NbtOps.INSTANCE, NbtCompound()).result().ifPresent { t ->
                tag.put("coverState", t)
            }
        }
        val list = NbtList()
        connections.forEach { (dir, conn) ->
            if (conn != BasePipeBlock.ConnectionType.NONE) {
                val t = NbtCompound()
                t.putByte("d", dir.id.toByte())
                t.putByte("c", conn.id.toByte())
                list.add(t)
            }
        }
        tag.put("connections", list)
    }

    fun sync() {
        Preconditions.checkNotNull(world) // Maintain distinct failure case from below
        check(world is ServerWorld) { "Cannot call sync() on the logical client! Did you check world.isClient first?" }
        (world as ServerWorld).chunkManager.markForUpdate(getPos())
    }

    override fun toUpdatePacket(): BlockEntityUpdateS2CPacket {
        return BlockEntityUpdateS2CPacket.create(this)
    }

    override fun toInitialChunkDataNbt(): NbtCompound {
        val nbt = super.toInitialChunkDataNbt()
        writeNbt(nbt)
        return nbt
    }

    data class PipeRenderData(val cover: BlockState?, val connections: Array<Direction>)
}
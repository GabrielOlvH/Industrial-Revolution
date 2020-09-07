package me.steven.indrev.blockentities.farms

import io.netty.buffer.Unpooled
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IConfig
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.math.Box

abstract class AOEMachineBlockEntity<T : IConfig>(tier: Tier, registry: MachineRegistry) : MachineBlockEntity<T>(tier, registry) {
    var renderWorkingArea = false
    abstract var range: Int
    open fun getWorkingArea(): Box {
        val box = Box(pos)
        return box.expand(range.toDouble(), 0.0, range.toDouble()).stretch(0.0, range.toDouble() * 2, 0.0)
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("range", range)
        return super.toTag(tag)
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("range", range)
        return super.toClientTag(tag)
    }

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        range = tag?.getInt("range") ?: range
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        range = tag?.getInt("range") ?: range
    }

    companion object {
        fun sendValueUpdatePacket(value: Int, ctx: ScreenHandlerContext) {
            if (value > 0) {
                val packet = PacketByteBuf(Unpooled.buffer())
                packet.writeInt(value)
                ctx.run { _, pos -> packet.writeBlockPos(pos) }
                ClientSidePacketRegistry.INSTANCE.sendToServer(UPDATE_VALUE_PACKET_ID, packet)
            }
        }

        val UPDATE_VALUE_PACKET_ID = identifier("update_value_packet")
    }
}
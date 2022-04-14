package me.steven.indrev.blockentities.farms

import io.netty.buffer.Unpooled
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.config.IConfig
import me.steven.indrev.packets.common.UpdateAOEMachineRangePacket
import me.steven.indrev.registry.MachineRegistry
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.block.BlockState
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box

abstract class AOEMachineBlockEntity<T : IConfig>(tier: Tier, registry: MachineRegistry, pos: BlockPos, state: BlockState) : MachineBlockEntity<T>(tier, registry, pos, state) {
    override val syncToWorld: Boolean = true

    var renderWorkingArea = false
    abstract var range: Int
    open fun getWorkingArea(): Box {
        val box = Box(pos)
        return box.expand(range.toDouble(), 0.0, range.toDouble()).stretch(0.0, range.toDouble() * 2, 0.0)
    }

    override fun toTag(tag: NbtCompound) {
        tag.putInt("range", range)
        super.toTag(tag)
    }

    override fun toClientTag(tag: NbtCompound) {
        tag.putInt("range", range)
    }

    override fun fromTag(tag: NbtCompound) {
        super.fromTag(tag)
        range = tag.getInt("range")
    }

    override fun fromClientTag(tag: NbtCompound) {
        range = tag.getInt("range")
    }

    companion object {
        fun sendValueUpdatePacket(value: Int, ctx: ScreenHandlerContext) {
            if (value > 0) {
                val packet = PacketByteBuf(Unpooled.buffer())
                packet.writeInt(value)
                ctx.run { _, pos -> packet.writeBlockPos(pos) }
                ClientPlayNetworking.send(UpdateAOEMachineRangePacket.UPDATE_VALUE_PACKET_ID, packet)
            }
        }


    }
}
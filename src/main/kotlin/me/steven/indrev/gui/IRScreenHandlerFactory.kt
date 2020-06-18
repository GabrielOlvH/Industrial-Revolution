package me.steven.indrev.gui

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.impl.screenhandler.ExtendedScreenHandlerType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class IRScreenHandlerFactory(val type: ExtendedScreenHandlerType<*>, val pos: BlockPos) : ExtendedScreenHandlerFactory {
    override fun createMenu(syncId: Int, inv: PlayerInventory?, player: PlayerEntity?): ScreenHandler? {
        val packet = PacketByteBuf(Unpooled.buffer())
        packet.writeBlockPos(pos)
        return type.create(syncId, inv, packet)
    }

    override fun writeScreenOpeningData(p0: ServerPlayerEntity?, p1: PacketByteBuf?) {
        p1?.writeBlockPos(pos)
    }

    override fun getDisplayName(): Text? = null

}
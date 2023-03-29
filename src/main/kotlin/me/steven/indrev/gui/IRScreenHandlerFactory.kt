package me.steven.indrev.gui

import me.steven.indrev.utils.literal
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

open class IRScreenHandlerFactory(
    private val handlerFactory: (Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler,
    private val pos: BlockPos
) : ExtendedScreenHandlerFactory {
    override fun createMenu(syncId: Int, inv: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        return handlerFactory(syncId, inv!!, ScreenHandlerContext.create(inv.player.world, pos))
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
    }

    override fun getDisplayName(): Text? = literal("test")

}
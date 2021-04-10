package me.steven.indrev.gui.screenhandlers.resreport

import me.steven.indrev.world.chunkveins.ChunkVeinData
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class ResourceReportScreenHandlerFactory(
    private val handlerFactory: (Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler,
    private val pos: BlockPos,
    private val veinData: ChunkVeinData
) : ExtendedScreenHandlerFactory {
    override fun createMenu(syncId: Int, inv: PlayerInventory, player: PlayerEntity): ScreenHandler =
        handlerFactory(syncId, inv, ScreenHandlerContext.create(inv.player.world, pos))

    override fun writeScreenOpeningData(player: ServerPlayerEntity?, buf: PacketByteBuf) {
        buf.writeBlockPos(pos)
        buf.writeIdentifier(veinData.veinIdentifier)
        buf.writeInt(veinData.explored)
        buf.writeInt(veinData.size)
    }

    override fun getDisplayName(): Text? = null
}
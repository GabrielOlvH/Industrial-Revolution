package me.steven.indrev.gui.controllers.pipes

import me.steven.indrev.networks.EndpointData
import me.steven.indrev.networks.Network
import me.steven.indrev.networks.item.ItemEndpointData
import me.steven.indrev.networks.item.ItemNetworkState
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class PipeFilterScreenFactory(
    private val handlerFactory: (Int, PlayerInventory) -> ScreenHandler,
    private val pos: BlockPos,
    private val dir: Direction
) : ExtendedScreenHandlerFactory {
    override fun createMenu(syncId: Int, inv: PlayerInventory?, player: PlayerEntity?): ScreenHandler {
        return handlerFactory(syncId, inv!!)
    }

    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        (Network.Type.ITEM.getNetworkState(player.serverWorld) as? ItemNetworkState)?.let { state ->
            val data = state.getEndpointData(pos, dir, true) as ItemEndpointData
            buf.writeEnumConstant(dir)
            buf.writeBlockPos(pos)
            data.filter.forEach { buf.writeItemStack(it) }
            buf.writeBoolean(data.whitelist)
            buf.writeBoolean(data.matchDurability)
            buf.writeBoolean(data.matchTag)
            buf.writeBoolean(data.type != EndpointData.Type.INPUT)
            if (data.type != EndpointData.Type.INPUT) {
                buf.writeEnumConstant(data.type)
                buf.writeEnumConstant(data.mode)
            }
        }
    }

    override fun getDisplayName(): Text = LiteralText("Item Filter")

}
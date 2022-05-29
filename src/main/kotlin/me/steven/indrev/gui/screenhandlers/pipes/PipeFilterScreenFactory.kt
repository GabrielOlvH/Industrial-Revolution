package me.steven.indrev.gui.screenhandlers.pipes

import me.steven.indrev.networks.Network
import me.steven.indrev.networks.item.ItemNetworkState
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import me.steven.indrev.utils.literal
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
        (Network.Type.ITEM.getNetworkState(player.world as ServerWorld) as? ItemNetworkState)?.let { state ->
            val data = state.getEndpointData(pos, dir, false)
            val filterData = state.getFilterData(pos, dir, true)
            buf.writeEnumConstant(dir)
            buf.writeBlockPos(pos)
            filterData.filter.forEach { buf.writeItemStack(it) }
            buf.writeBoolean(filterData.whitelist)
            buf.writeBoolean(filterData.matchDurability)
            buf.writeBoolean(filterData.matchTag)
            buf.writeBoolean(data != null)
            if (data != null) {
                buf.writeEnumConstant(data.type)
                buf.writeEnumConstant(data.mode)
            }
        }
    }

    override fun getDisplayName(): Text = literal("Item Filter")

}
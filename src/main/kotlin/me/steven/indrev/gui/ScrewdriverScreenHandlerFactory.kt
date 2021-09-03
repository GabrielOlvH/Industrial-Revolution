package me.steven.indrev.gui

import me.steven.indrev.api.sideconfigs.ConfigurationType
import me.steven.indrev.blockentities.MachineBlockEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandler
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos

class ScrewdriverScreenHandlerFactory(
    handlerFactory: (Int, PlayerInventory, ScreenHandlerContext) -> ScreenHandler,
    pos: BlockPos,
    val blockEntity: MachineBlockEntity<*>
) : IRScreenHandlerFactory(handlerFactory, pos) {
    override fun writeScreenOpeningData(player: ServerPlayerEntity, buf: PacketByteBuf) {
        super.writeScreenOpeningData(player, buf)

        buf.writeBoolean(blockEntity.isConfigurable(ConfigurationType.ITEM))
        if (blockEntity.isConfigurable(ConfigurationType.ITEM)) {
            blockEntity.getCurrentConfiguration(ConfigurationType.ITEM).writeBuf(buf)
        }
        buf.writeBoolean(blockEntity.isConfigurable(ConfigurationType.FLUID))
        if (blockEntity.isConfigurable(ConfigurationType.FLUID)) {
            blockEntity.getCurrentConfiguration(ConfigurationType.FLUID).writeBuf(buf)
        }
        buf.writeBoolean(blockEntity.isConfigurable(ConfigurationType.ENERGY))
        if (blockEntity.isConfigurable(ConfigurationType.ENERGY)) {
            blockEntity.getCurrentConfiguration(ConfigurationType.ENERGY).writeBuf(buf)
        }
    }
}
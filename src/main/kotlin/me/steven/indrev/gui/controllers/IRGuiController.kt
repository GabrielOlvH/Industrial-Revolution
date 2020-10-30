package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatch
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.properties
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier


open class IRGuiController(
    type: ScreenHandlerType<*>?,
    syncId: Int,
    playerInventory: PlayerInventory,
    val ctx: ScreenHandlerContext
) : SyncedGuiDescription(type, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx)) {
    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        super.addPainters()
        val offset = 170 - rootPanel.width
        rootPanel.backgroundPainter =
            BackgroundPainter.createLightDarkVariants(
                NinePatch(Identifier("libgui", "textures/widget/panel_light.png")).setPadding(8)
                    .setRightPadding(offset),
                NinePatch(Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8)
                    .setRightPadding(offset)
            )
    }

    override fun sendContentUpdates() {
        if (playerInventory.player is ServerPlayerEntity) {
            for (i in properties.indices) {
                val property = properties[i]
                if (property.hasChanged()) {
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeInt(syncId)
                    buf.writeInt(i)
                    buf.writeInt(property.get())
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerInventory.player, IndustrialRevolution.SYNC_PROPERTY, buf)
                }
            }
        }
        super.sendContentUpdates()
    }
}
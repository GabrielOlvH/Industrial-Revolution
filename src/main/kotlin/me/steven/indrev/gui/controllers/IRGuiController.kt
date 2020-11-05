package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatch
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
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

    private val values = Array(propertyDelegate.size()) { -1 }

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
            for (i in 0 until propertyDelegate.size()) {
                if (values[i] != propertyDelegate[i]) {
                    values[i] = propertyDelegate[i]
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeInt(syncId)
                    buf.writeInt(i)
                    buf.writeInt(values[i])
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer(playerInventory.player, IndustrialRevolution.SYNC_PROPERTY, buf)
                }
            }
        }
        super.sendContentUpdates()
    }
}
package me.steven.indrev.gui.screenhandlers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.netty.buffer.Unpooled
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.utils.properties
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.network.PacketByteBuf
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

open class IRGuiScreenHandler(
    type: ScreenHandlerType<*>?,
    syncId: Int,
    playerInventory: PlayerInventory,
    val ctx: ScreenHandlerContext
) : SyncedGuiDescription(type, syncId, playerInventory, getBlockInventory(ctx), getBlockPropertyDelegate(ctx)) {

    init {
        (properties as ArrayList).clear()
    }

    private val values = Array(propertyDelegate.size()) { -1 }

    @Environment(EnvType.CLIENT)
    override fun addPainters() {
        super.addPainters()
        val offset = 170 - rootPanel.width
        rootPanel.backgroundPainter =
            BackgroundPainter.createLightDarkVariants(
                BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_light.png")).setPadding(8)
                    .setRightPadding(offset),
                BackgroundPainter.createNinePatch(Identifier("libgui", "textures/widget/panel_dark.png")).setPadding(8)
                    .setRightPadding(offset)
            )
    }

    override fun sendContentUpdates() {
        val player = playerInventory.player
        if (player is ServerPlayerEntity) {
            for (i in 0 until propertyDelegate.size()) {
                if (values[i] != propertyDelegate[i]) {
                    values[i] = propertyDelegate[i]
                    val buf = PacketByteBuf(Unpooled.buffer())
                    buf.writeInt(syncId)
                    buf.writeInt(i)
                    buf.writeInt(values[i])
                    ServerPlayNetworking.send(player, IndustrialRevolution.SYNC_PROPERTY, buf)
                }
            }
        }
        super.sendContentUpdates()
    }
}
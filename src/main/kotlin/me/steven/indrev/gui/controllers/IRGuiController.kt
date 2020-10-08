package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.NinePatch
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.screen.ScreenHandlerType
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
}
package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.util.math.MatrixStack

class WPlayerRender : WWidget() {
    override fun paint(ctx:DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        InventoryScreen.drawEntity(ctx, x, y, 30, -mouseX.toFloat(), -mouseY.toFloat(), MinecraftClient.getInstance().player)
    }
}
package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.client.util.math.MatrixStack

class WPlayerRender : WWidget() {
    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        InventoryScreen.drawEntity(x, y, 30, -mouseX.toFloat(), -mouseY.toFloat(), MinecraftClient.getInstance().player)
    }
}
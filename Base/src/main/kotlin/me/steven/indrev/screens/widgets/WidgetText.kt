package me.steven.indrev.screens.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text

class WidgetText(val text: () -> Text) : Widget() {
    override var height: Int = 18
    override var width: Int = 18

    override fun draw(matrices: MatrixStack, x: Int, y: Int) {
        MinecraftClient.getInstance().textRenderer.draw(matrices, text(), x.toFloat(), y.toFloat(), 0x404040)
    }
}
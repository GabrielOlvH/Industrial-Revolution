package me.steven.indrev.screens.widgets

import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

class WidgetText(val text: () -> Text) : Widget() {
    override var height: Int = 18
    override var width: Int = 18

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        ctx.drawText(MinecraftClient.getInstance().textRenderer, text(), x, y, 0x404040, false)
    }
}
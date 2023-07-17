package me.steven.indrev.screens.widgets

import net.minecraft.client.gui.DrawContext
import net.minecraft.util.Identifier

open class WidgetSprite(private val id: Identifier, override var width: Int, override var height: Int): Widget() {
    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        ctx.drawTexture(id, x, y, 0f, 0f, width, height, width, height)
    }

}
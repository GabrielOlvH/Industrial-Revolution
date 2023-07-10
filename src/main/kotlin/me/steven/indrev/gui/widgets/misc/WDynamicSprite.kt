package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class WDynamicSprite(val provider: () -> Identifier) : WWidget() {
    override fun paint(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(ctx, x, y, width, height, provider(), -1)
    }
}
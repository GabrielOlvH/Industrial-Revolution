package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class WDynamicSprite(val provider: () -> Identifier) : WWidget() {
    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, provider(), -1)
    }
}
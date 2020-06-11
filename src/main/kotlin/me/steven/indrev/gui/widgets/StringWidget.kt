package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Alignment
import net.minecraft.client.util.math.MatrixStack

class StringWidget(private val string: () -> String, private val color: Int) : WWidget() {
    constructor(string: String, color: Int) : this({ string }, color)

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.drawString(matrices, string(), Alignment.CENTER, x, y, this.width, color)
    }
}
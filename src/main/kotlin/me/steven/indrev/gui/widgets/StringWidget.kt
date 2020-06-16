package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Alignment
import net.minecraft.client.util.math.MatrixStack

class StringWidget(
    private val string: () -> String,
    private val color: Int,
    private val alignment: Alignment = Alignment.CENTER
) : WWidget() {
    constructor(string: String, color: Int, alignment: Alignment = Alignment.CENTER) : this(
        { string },
        color,
        alignment
    )

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.drawString(matrices, string(), alignment, x, y, this.width, color)
    }
}
package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.Alignment
import java.awt.Color

class StringWidget(private val string: String) : WWidget() {
    override fun paintBackground(x: Int, y: Int) {
        ScreenDrawing.drawString(string, Alignment.CENTER, x, y, this.width, Color.BLACK.rgb)
    }
}
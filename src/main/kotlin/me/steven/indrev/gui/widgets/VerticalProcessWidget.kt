package me.steven.indrev.gui.widgets

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PropertyDelegate
import kotlin.math.round

class VerticalProcessWidget(private val delegate: PropertyDelegate) : WWidget() {
    init {
        this.setSize(17, 24)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, PROCESS_EMPTY, -1)
        val burnTime = delegate[2]
        val maxBurnTime = 1200
        if (burnTime > 0) {
            val v = 1f - (((burnTime.toFloat() * 23 / maxBurnTime) + 1) / 24)
            val h = round(v * height).toInt()
            ScreenDrawing.texturedRect(x, y, width, h, PROCESS_FULL, 0f, 0f, 1f, v, -1)
        }
    }

    companion object {
        private val PROCESS_EMPTY =
            identifier("textures/gui/widget_processing_empty_vertical.png")
        private val PROCESS_FULL =
            identifier("textures/gui/widget_processing_full_vertical.png")
    }
}
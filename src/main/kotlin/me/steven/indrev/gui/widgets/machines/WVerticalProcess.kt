package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PropertyDelegate

class WVerticalProcess(private val delegate: PropertyDelegate) : WWidget() {
    init {
        this.setSize(17, 24)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, PROCESS_EMPTY, -1)
        val processTime = 1200 - delegate[2]
        // only used by one machine so yea this is hardcoded
        val maxProcessTime = 1200
        if (processTime > 0) {
            var percent = processTime.toFloat() / maxProcessTime.toFloat()
            percent = (percent * height).toInt() / height.toFloat()
            val barSize = (height * percent).toInt()
            if (barSize > 0)
                ScreenDrawing.texturedRect(
                    x, y, width, height - barSize,
                    PROCESS_FULL, 0f, 0f, 1f, 1f - percent, -1
                )
        }
    }

    companion object {
        private val PROCESS_EMPTY =
            identifier("textures/gui/widget_processing_empty_vertical.png")
        private val PROCESS_FULL =
            identifier("textures/gui/widget_processing_full_vertical.png")
    }
}
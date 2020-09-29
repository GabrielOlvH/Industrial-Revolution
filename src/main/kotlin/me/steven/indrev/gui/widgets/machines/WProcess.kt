package me.steven.indrev.gui.widgets.machines

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PropertyDelegate
import net.minecraft.text.TranslatableText

class WProcess(private val delegate: PropertyDelegate, val process: Int = 3, val maxProcess: Int = 4) : WWidget() {
    init {
        this.setSize(24, 17)
    }

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, PROCESS_EMPTY, -1)
        val maxProcessTime = delegate[maxProcess]
        val processTime = maxProcessTime - delegate[process]
        if (processTime > 0) {
            var percent = processTime.toFloat() / maxProcessTime.toFloat()
            percent = (percent * width).toInt() / width.toFloat()
            val barSize = (width * percent).toInt()
            if (barSize > 0)
                ScreenDrawing.texturedRect(
                    x, y, barSize, height,
                    PROCESS_FULL, 0f, 0f, percent, 1f, -1
                )
        }
    }

    override fun addTooltip(tooltip: TooltipBuilder?) {
        val maxProcessTime = delegate[maxProcess]
        val processTime = maxProcessTime - delegate[process]
        if (maxProcessTime > processTime) {
            val percent = processTime * 100 / maxProcessTime
            tooltip?.add(TranslatableText("gui.widget.process", "${percent}%"))
        }
    }

    companion object {
        val PROCESS_EMPTY =
            identifier("textures/gui/widget_processing_empty.png")
        val PROCESS_FULL =
            identifier("textures/gui/widget_processing_full.png")
        val PROCESS_VERTICAL_EMPTY =
            identifier("textures/gui/widget_processing_empty_vertical.png")
        val PROCESS_VERTICAL_FULL =
            identifier("textures/gui/widget_processing_full_vertical.png")
    }
}
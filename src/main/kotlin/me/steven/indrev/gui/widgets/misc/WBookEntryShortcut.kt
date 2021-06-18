package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.data.InputResult
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack

open class WBookEntryShortcut : WButton() {

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(matrices, x, y, width, height, ICON_IDENTIFIER, -1)
    }

    override fun onMouseDown(x: Int, y: Int, button: Int): InputResult {
        onClick(x, y, button)
        return InputResult.PROCESSED
    }

    override fun setSize(x: Int, y: Int) {
        this.width = x
        this.height = y
    }

    companion object {
        private val ICON_IDENTIFIER = identifier("textures/gui/help.png")
    }
}
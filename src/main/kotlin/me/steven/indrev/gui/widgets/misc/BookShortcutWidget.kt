package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WButton
import me.steven.indrev.utils.identifier
import net.minecraft.client.util.math.MatrixStack

open class BookShortcutWidget : WButton() {

    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        ScreenDrawing.texturedRect(x, y, width, height, ICON_IDENTIFIER, -1)
    }

    override fun setSize(x: Int, y: Int) {
        this.width = x
        this.height = y
    }

    companion object {
        private val ICON_IDENTIFIER = identifier("textures/gui/help.png")
    }
}
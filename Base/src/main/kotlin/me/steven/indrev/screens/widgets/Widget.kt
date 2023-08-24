package me.steven.indrev.screens.widgets

import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawContext
import net.minecraft.text.Text

abstract class Widget {
    abstract var width: Int
    abstract var height: Int

    var x: Int = 0
    var y: Int = 0

    var tooltipBuilder: ((MutableList<Text>) -> Unit)? = null

    var highlight: () -> Boolean = { false }

    open fun draw(ctx: DrawContext, x: Int, y: Int) {

    }

    open fun drawMouseover(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {

    }

    open fun drawHighlight(ctx: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        if (highlight()) {

            ctx.matrices.push()
            ctx.drawTexture(identifier("textures/gui/widgets/widget_warning_highlight.png"), x - 3, y - 3, 0f, 0f, width + 6, height + 6, 128, 128)

            ctx.matrices.pop()
        }
    }

    fun setTooltip(builder: (MutableList<Text>) -> Unit): Widget {
        this.tooltipBuilder = builder
        return this
    }

    open fun onClick(mouseX: Double, mouseY: Double, button: Int) {

    }

    open fun validate(handler: MachineScreenHandler) {
    }

    fun isPointWithinBounds(
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        pointX: Double,
        pointY: Double
    ): Boolean {
        var pointX = pointX
        var pointY = pointY

        return pointX >= (x - 1).toDouble() && pointX < (x + width + 1).toDouble() && pointY >= (y - 1).toDouble() && pointY < (y + height + 1).toDouble()
    }

}
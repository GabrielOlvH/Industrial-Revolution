package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.drawCircle
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.*

open class WCircleProgressBar(val value: () -> Int, val max: () -> Int, val colorProvider: (Int, Int) -> Int) : WWidget() {


    override fun paint(context: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        val matrices = context.matrices
        matrices.push()
        matrices.translate(0.0, 0.0, 300.0)

        drawCircle(context, value(), max(), x, y, width, colorProvider)

        matrices.pop()
    }
}
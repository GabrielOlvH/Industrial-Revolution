package me.steven.indrev.gui.widgets.misc

import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WWidget
import me.steven.indrev.utils.drawCircle
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.*

open class WCircleProgressBar(val value: () -> Int, val max: () -> Int, val colorProvider: (Int, Int) -> Int) : WWidget() {

    override fun paint(matrices: MatrixStack, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        matrices.push()
        matrices.translate(0.0, 0.0, 300.0)

        drawCircle(matrices, value(), max(), x, y, width, colorProvider)

        matrices.pop()
    }
}
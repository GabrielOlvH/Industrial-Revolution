package me.steven.indrev.screens.widgets

import me.steven.indrev.screens.machine.MachineScreenHandler
import net.minecraft.client.util.math.MatrixStack

class WidgetGroup(val widgets: List<Widget>) : Widget() {
    override var width: Int = widgets.maxOf { it.width }
    override var height: Int = widgets.maxOf { it.height }

    override fun draw(matrices: MatrixStack, x: Int, y: Int) {
        widgets.forEach { it.draw(matrices, x + it.x, y + it.y) }
    }

    override fun validate(handler: MachineScreenHandler) {
        widgets.forEach {
            it.x += x
            it.y += y
            it.validate(handler)
            it.x -= x
            it.y -= y
        }
    }
}
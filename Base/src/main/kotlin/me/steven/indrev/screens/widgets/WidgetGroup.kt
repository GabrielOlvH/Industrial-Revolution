package me.steven.indrev.screens.widgets

import me.steven.indrev.screens.machine.MachineScreenHandler
import net.minecraft.client.gui.DrawContext

open class WidgetGroup(val widgets: List<Widget>) : Widget() {
    override var width: Int = widgets.maxOf { it.width }
    override var height: Int = widgets.maxOf { it.height }

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        widgets.forEach { it.draw(ctx, x + it.x, y + it.y) }
    }

    override fun drawMouseover(ctx: DrawContext, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        widgets.forEach {
            if (it.isPointWithinBounds(x + it.x, y + it.y, it.width, it.height, mouseX.toDouble(), mouseY.toDouble()))
                it.drawMouseover(ctx, x + it.x, y + it.y, mouseX, mouseY)
        }
    }

    override fun drawHighlight(ctx: DrawContext, x: Int, y: Int, width: Int, height: Int) {
        widgets.forEach { w -> w.drawHighlight(ctx, x+w.x, y + w.y, w.width, w.height)}
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
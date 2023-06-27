package me.steven.indrev.screens.machine

import me.steven.indrev.screens.widgets.Widget
import me.steven.indrev.screens.widgets.WidgetGroup
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import java.util.*
import kotlin.math.pow

class MachineHandledScreen<T : MachineScreenHandler>(handler: T, playerInventory: PlayerInventory, title: Text) : HandledScreen<T>(handler, playerInventory, title) {

    init {
        this.backgroundWidth = 194
        this.backgroundHeight = 201
        this.playerInventoryTitleY = this.backgroundHeight - 112
        this.titleY = 0
        this.titleX = (this.backgroundWidth / 2) - (MinecraftClient.getInstance().textRenderer.getWidth(this.getTitle())/2) - 8
    }

    override fun drawBackground(matrices: MatrixStack, delta: Float, mouseX: Int, mouseY: Int) {
        renderBackground(matrices)
        handler.widgets.forEach { w -> w.draw(matrices, w.x + x, w.y + y) }
    }

    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) {
        textRenderer.draw(matrices, title, titleX.toFloat(), titleY.toFloat(), 0xECECEC)
        textRenderer.draw(matrices, playerInventoryTitle, playerInventoryTitleX.toFloat(), playerInventoryTitleY.toFloat(), 0xECECEC)
    }

    override fun isClickOutsideBounds(mouseX: Double, mouseY: Double, left: Int, top: Int, button: Int): Boolean {
        return false
    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        handler.tick()
    }

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(matrices, mouseX, mouseY, delta)
        val w = getWidgetAt(handler.widgets.reversed(), mouseX.toDouble(), mouseY.toDouble())
        val data = w?.tooltipData?.invoke()
        if (w?.tooltipBuilder != null || data != null) {
            val tooltip = mutableListOf<Text>()
            w.tooltipBuilder?.invoke(tooltip)
            if (tooltip.isNotEmpty() || data != null) renderTooltip(matrices, tooltip, Optional.ofNullable(data), mouseX, mouseY)
        }
        w?.drawMouseover(matrices, w.x + x, w.y + y, mouseX, mouseY)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        super.mouseClicked(mouseX, mouseY, button)
        getWidgetAt(handler.widgets.reversed(), mouseX, mouseY)?.onClick(mouseX, mouseY, button)
        return true
    }

    private fun getWidgetAt(widgets: List<Widget>, mouseX: Double, mouseY: Double): Widget? {
        widgets.forEach { w ->
            if (isPointWithinBounds(w.x, w.y, w.width, w.height, mouseX, mouseY)) {
                if (w is WidgetGroup) {
                    val widgetAt = getWidgetAt(w.widgets.reversed(), mouseX - w.x, mouseY - w.y)
                    if (widgetAt != null) return widgetAt
                } else return w
            }
        }
        return null
    }
}
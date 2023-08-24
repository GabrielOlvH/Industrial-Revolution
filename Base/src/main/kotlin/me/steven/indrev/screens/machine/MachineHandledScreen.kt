package me.steven.indrev.screens.machine

import me.steven.indrev.screens.widgets.Widget
import me.steven.indrev.screens.widgets.WidgetGroup
import me.steven.indrev.screens.widgets.WidgetSprite
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import java.util.*
import kotlin.math.pow

class MachineHandledScreen<T : MachineScreenHandler>(handler: T, playerInventory: PlayerInventory, title: Text) : HandledScreen<T>(handler, playerInventory, title) {



    init {
        this.backgroundWidth = 194
        this.backgroundHeight = 201
        this.playerInventoryTitleY = this.backgroundHeight - 112
        this.titleY = 0
        this.titleX = (this.backgroundWidth / 2) - (MinecraftClient.getInstance().textRenderer.getWidth(this.getTitle())/2) - 8

        handler.background.add(WidgetSprite(MachineScreenHandler.DEFAULT_BG, 194, 201).also {
            it.x = -8
            it.y = -11
        })
    }


    var lastProgress = 0.0

    override fun drawBackground(ctx: DrawContext, delta: Float, mouseX: Int, mouseY: Int) {
        renderBackground(ctx)

        val c1 = 1.70158
        val c3 = c1 + 1

        handler.ioHelper.ioConfig.x = 18+(-118 * (1 + c3 * (lastProgress-1).pow(3.0) + c1 * (lastProgress-1).pow(2))).toInt()
        lastProgress = MathHelper.lerp(delta.toDouble(), lastProgress, handler.ioHelper.openIoConfigAnimationProgress)

        handler.widgets.forEach { w -> w.drawHighlight(ctx, w.x + x, w.y + y, w.width, w.height) }
        handler.widgets.forEach { w -> w.draw(ctx, w.x + x, w.y + y) }
    }

    override fun renderBackground(ctx: DrawContext) {
        super.renderBackground(ctx)
        handler.background.forEach { w -> w.draw(ctx, x + w.x, y + w.y) }
    }

    override fun drawForeground(ctx: DrawContext, mouseX: Int, mouseY: Int) {
        ctx.drawText(textRenderer, title, titleX, titleY, 0xECECEC, false)
        ctx.drawText(textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, 0xECECEC, false)
    }

    override fun isClickOutsideBounds(mouseX: Double, mouseY: Double, left: Int, top: Int, button: Int): Boolean {
        return false
    }

    override fun handledScreenTick() {
        super.handledScreenTick()
        handler.ioHelper.tick()
    }

    override fun render(ctx: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(ctx, mouseX, mouseY, delta)
        val w = handler.widgets.reversed().firstOrNull { w -> isPointWithinBounds(w.x, w.y, w.width, w.height, mouseX.toDouble(), mouseY.toDouble()) }
        if (w?.tooltipBuilder != null ) {
            val tooltip = mutableListOf<Text>()
            w.tooltipBuilder?.invoke(tooltip)
            if (tooltip.isNotEmpty()) ctx.drawTooltip(textRenderer, tooltip,  mouseX, mouseY)
        }
        w?.drawMouseover(ctx, x+w.x, y+w.y, mouseX, mouseY)
        drawMouseoverTooltip(ctx, mouseX, mouseY)
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
package me.steven.indrev.screens.widgets

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.InventoryScreen
import net.minecraft.entity.LivingEntity

class WidgetEntity(val entity: () -> LivingEntity, val followCursor: Boolean) : Widget() {
    override var width: Int = 3*18-1
    override var height: Int = 18*4-1

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        ctx.fill(x, y, x + width, y + height, 0xFF222222.toInt())
        ctx.fill(x + 2, y + 2, x + width - 2, y + height - 2, 0xFF000000.toInt())
        InventoryScreen.drawEntity(ctx, x + width / 2, y + height - 5, 30, 0f, 0f, entity())
    }
}
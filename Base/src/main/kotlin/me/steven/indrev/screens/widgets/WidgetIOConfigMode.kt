package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.utils.ConfigurationTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.item.ItemStack

class WidgetIOConfigMode(val type: ConfigurationTypes, var selected: Boolean, var shown: Boolean) : Widget() {
    override var width: Int = 18
    override var height: Int = 18
    var enabled = false

    var click: (mouseX: Double, mouseY: Double, button: Int) -> Unit = { _, _, _ -> }

    init {
        this.tooltipBuilder = { tooltip ->
            if (!enabled) {
                tooltip.add(type.upgradeText)
            } else {
                tooltip.add(type.text)
            }
        }
    }

    override fun draw(ctx: DrawContext, x: Int, y: Int) {
        if (!enabled) {
            ctx.fill(x, y, x + width, y + height, 0xFFFF0000.toInt())
        }
        if (selected) {
            ctx.fill(x, y, x + width, y + height, 0xFF000099.toInt())
            ctx.fill(x + 1, y + 1, x + width - 1, y + height - 1, 0xFF444499.toInt())
        }
        ctx.drawItemInSlot(MinecraftClient.getInstance().textRenderer, ItemStack(type.icon), x + 1, y + 1)
    }

    override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        if (shown && enabled)
            click(mouseX, mouseY, button)
    }

}
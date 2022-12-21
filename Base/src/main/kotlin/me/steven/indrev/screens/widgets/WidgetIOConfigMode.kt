package me.steven.indrev.screens.widgets

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.utils.ConfigurationTypes
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.util.math.MatrixStack
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

    override fun draw(matrices: MatrixStack, x: Int, y: Int) {
        if (shown) {
            RenderSystem.enableDepthTest()
            MinecraftClient.getInstance().itemRenderer.renderInGui(ItemStack(type.icon), x + 1, y + 1)

            if (!enabled) {
                DrawableHelper.fill(matrices, x, y, x + width, y + height, 0xFFFF0000.toInt())
            }

            if (selected) {
                DrawableHelper.fill(matrices, x, y, x + width, y + height, 0xFF000099.toInt())
                DrawableHelper.fill(matrices, x+1, y+1, x + width -1, y + height-1, 0xFF444499.toInt())
            }
        }
    }

    override fun onClick(mouseX: Double, mouseY: Double, button: Int) {
        if (shown && enabled)
            click(mouseX, mouseY, button)
    }

}
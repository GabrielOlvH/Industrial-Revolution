package me.steven.indrev.gui.tooltip.modular

import me.steven.indrev.gui.tooltip.energy.EnergyTooltipComponent
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack
import kotlin.math.ceil

class ModularTooltipComponent(private val data: ModularTooltipData) : EnergyTooltipComponent(data) {

    override fun getHeight(): Int = if (Screen.hasShiftDown()) 18 else 18 + 18 * (ceil(data.modules.size / 5.0).toInt())

    override fun getWidth(textRenderer: TextRenderer): Int {
        val energyWidth = super.getWidth(textRenderer)
        var cX = 0
        data.modules.forEachIndexed { index, module ->
            val level = data.levelProvider(module)
            cX += (level * 5) + 18
            if (index + 1 % 5 == 0)
                return cX
        }
        return cX.coerceAtLeast(energyWidth)
    }

    override fun drawItems(textRenderer: TextRenderer, x: Int, y: Int, ctx: DrawContext) {
        super.drawItems(textRenderer, x, y, ctx)
        if (Screen.hasShiftDown()) return

        var cX = x
        var cY = y + 18
        data.modules.sortedByDescending { data.levelProvider(it) }.forEachIndexed { index, module ->
            val level = data.levelProvider(module)
            cX += level * 5
            repeat(level) {
                cX -= 5
                ctx.drawItem(ItemStack(module.item.asItem()), cX, cY)
            }
            cX += (level * 5) + 18
            if (index + 1 % 5 == 0) {
                cY += 18
                cX = x
            }
        }
    }
}
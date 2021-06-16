package me.steven.indrev.gui.tooltip.modular

import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.tooltip.TooltipComponent
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.texture.TextureManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.item.ItemStack

class ModularTooltipComponent(val data: ModularTooltipData) : TooltipComponent {

    override fun getHeight(): Int = if (Screen.hasShiftDown()) 0 else 18

    override fun getWidth(textRenderer: TextRenderer?): Int {
        var cX = 0
        data.modules.forEachIndexed { index, module ->
            val level = data.levelProvider(module)
            cX += (level * 5) + 18
            if (index + 1 % 5 == 0)
                return cX
        }
        return cX
    }

    override fun drawItems(
        textRenderer: TextRenderer,
        x: Int,
        y: Int,
        matrices: MatrixStack,
        itemRenderer: ItemRenderer,
        z: Int,
        textureManager: TextureManager
    ) {
        if (Screen.hasShiftDown()) return

        var cX = x
        var cY = y
        data.modules.sortedByDescending { data.levelProvider(it) }.forEachIndexed { index, module ->
            val level = data.levelProvider(module)
            cX += level * 5
            repeat(level) {
                cX -= 5
                itemRenderer.renderInGui(ItemStack(module.item.asItem()), cX, cY)
            }
            cX += (level * 5) + 18
            if (index + 1 % 5 == 0) {
                cY += 18
                cX = x
            }
        }
    }
}
package me.steven.indrev.gui.widgets.misc

import com.mojang.blaze3d.systems.RenderSystem
import io.github.cottonmc.cotton.gui.widget.WWidget
import net.minecraft.client.render.*
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Matrix4f

class WStaticTooltip : WWidget() {
    override fun paint(matrices: MatrixStack?, x: Int, y: Int, mouseX: Int, mouseY: Int) {
        drawTooltipBackground(matrices ?: return, x, y, width, height)
    }

    @Suppress("DEPRECATION")
    private fun drawTooltipBackground(matrices: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        bufferBuilder.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        val matrix4f = matrices.peek().positionMatrix
        val z = 0
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 4, x + width + 3, y - 3, z, -267386864, -267386864)
        fillGradient(matrix4f, bufferBuilder, x - 3, y + height + 3, x + width + 3, y + height + 4, z, -267386864, -267386864)
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + width + 3, y + height + 3, z, -267386864, -267386864)
        fillGradient(matrix4f, bufferBuilder, x - 4, y - 3, x - 3, y + height + 3, z, -267386864, -267386864)
        fillGradient(matrix4f, bufferBuilder, x + width + 3, y - 3, x + width + 4, y + height + 3, z, -267386864, -267386864)
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, z, 1347420415, 1344798847)
        fillGradient(matrix4f, bufferBuilder, x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, z, 1347420415, 1344798847)
        fillGradient(matrix4f, bufferBuilder, x - 3, y - 3, x + width + 3, y - 3 + 1, z, 1347420415, 1347420415)
        fillGradient(matrix4f, bufferBuilder, x - 3, y + height + 2, x + width + 3, y + height + 3, z, 1344798847, 1344798847)
        RenderSystem.enableDepthTest()
        RenderSystem.disableTexture()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        tessellator.draw()
        RenderSystem.disableBlend()
        RenderSystem.enableTexture()
    }

    private fun fillGradient(matrix4f: Matrix4f?, bufferBuilder: BufferBuilder, xStart: Int, yStart: Int, xEnd: Int, yEnd: Int, i: Int, j: Int, k: Int) {
        val f = (j shr 24 and 255).toFloat() / 255.0f
        val g = (j shr 16 and 255).toFloat() / 255.0f
        val h = (j shr 8 and 255).toFloat() / 255.0f
        val l = (j and 255).toFloat() / 255.0f
        val m = (k shr 24 and 255).toFloat() / 255.0f
        val n = (k shr 16 and 255).toFloat() / 255.0f
        val o = (k shr 8 and 255).toFloat() / 255.0f
        val p = (k and 255).toFloat() / 255.0f
        bufferBuilder.vertex(matrix4f, xEnd.toFloat(), yStart.toFloat(), i.toFloat()).color(g, h, l, f).next()
        bufferBuilder.vertex(matrix4f, xStart.toFloat(), yStart.toFloat(), i.toFloat()).color(g, h, l, f).next()
        bufferBuilder.vertex(matrix4f, xStart.toFloat(), yEnd.toFloat(), i.toFloat()).color(n, o, p, m).next()
        bufferBuilder.vertex(matrix4f, xEnd.toFloat(), yEnd.toFloat(), i.toFloat()).color(n, o, p, m).next()
    }
}
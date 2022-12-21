package me.steven.indrev.transportation.client

import com.mojang.blaze3d.systems.RenderSystem
import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3f

object NetworkPathRenderer : WorldRenderEvents.AfterTranslucent {
    override fun afterTranslucent(ctx: WorldRenderContext) {
        ClientPipeNetworkData.pathsToRender.forEach { (path, time) ->
            val cameraPos = MinecraftClient.getInstance().gameRenderer.camera.pos
            val matrices = ctx.matrixStack()
            matrices.push()
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)
            val it = path.nodes.iterator()
            var c = it.next()
            while (it.hasNext()) {
                val n = it.next()
                val alpha = time / ClientPipeNetworkData.PATH_RENDER_TIME.toFloat()
                render(ctx, BlockPos.fromLong(c), BlockPos.fromLong(n), 0x49c5f2, alpha.coerceAtMost(.5f))
                c = n
            }
            matrices.pop()
        }
    }

    fun render(context: WorldRenderContext, pos1: BlockPos, pos2: BlockPos, color: Int, alpha: Float) {

        val start = Vec3f(pos1.x + 0.5f, pos1.y + 0.5f, pos1.z + 0.5f)
        val end = Vec3f(pos2.x + 0.5f, pos2.y + 0.5f, pos2.z + 0.5f)

        val red = ColorHelper.Argb.getRed(color) / 255f
        val green = ColorHelper.Argb.getGreen(color) / 255f
        val blue = ColorHelper.Argb.getBlue(color) / 255f

        val width =  0.15f

        RenderSystem.setShader(GameRenderer::getRenderTypeLinesShader)
        RenderSystem.disableCull()
        RenderSystem.enableBlend()
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
        val buffer = Tessellator.getInstance().buffer
        val matrix4f = context.matrixStack().peek().positionMatrix

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
        buffer.vertex(matrix4f, start.x - width, start.y + width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   - width, start.y + width, end.z   - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   + width, start.y + width, end.z   + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   - width, start.y - width, end.z   - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   + width, start.y - width, end.z   + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y - width, start.z + width).color(red, green, blue, alpha).next()

        buffer.vertex(matrix4f, start.x + width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y - width, end.z   - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, end.z   + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y - width, end.z   - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y + width, end.z   + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y + width, start.z + width).color(red, green, blue, alpha).next()

        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   - width, start.y - width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   + width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   - width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   + width, start.y + width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z - width).color(red, green, blue, alpha).next()

        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   - width, end.y   - width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   + width, end.y   + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   - width, end.y   - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, end.x   + width, end.y   + width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z - width).color(red, green, blue, alpha).next()

        buffer.vertex(matrix4f, start.x + width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, end.y   - width, end.z   - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, end.y   + width, end.z   + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x + width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y - width, start.z - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, end.y   - width, end.z   - width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, end.y   + width, end.z   + width).color(red, green, blue, alpha).next()
        buffer.vertex(matrix4f, start.x - width, start.y + width, start.z + width).color(red, green, blue, alpha).next()
        Tessellator.getInstance().draw()

    }
}
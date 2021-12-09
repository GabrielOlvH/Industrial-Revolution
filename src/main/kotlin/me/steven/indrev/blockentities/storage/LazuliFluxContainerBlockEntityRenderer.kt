package me.steven.indrev.blockentities.storage

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.sin

class LazuliFluxContainerBlockEntityRenderer : BlockEntityRenderer<LazuliFluxContainerBlockEntity> {
    override fun render(
        entity: LazuliFluxContainerBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        entity ?: return
        val width = ((entity.energy.toFloat() / entity.getCapacity().toFloat()) * 0.5f) + 0.25f
        val sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(identifier("block/lazuli_flux_container_lf_level"))
        val color: Long = (255 shl 24 or when (entity.tier) {
            Tier.MK1 -> 0xffbb19
            Tier.MK2 -> 0x5d3dff
            Tier.MK3 -> 0xfd47ff
            else -> 0xff4070
        }).toLong()
        val maxX = floor((width * 16)) / 16f
        val time = entity.world!!.time
        matrices?.run {
            val offset = 0.001
            push()
            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.NORTH, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.EAST, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.SOUTH, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.WEST, width, tickDelta, time)
            translate(0.0, 0.0, offset)
            pop()
        }
    }

    private fun drawOverlay(matrices: MatrixStack, x1: Float, y1: Float, x2: Float, y2: Float, color: Long, sprite: Sprite, vertexConsumers: VertexConsumerProvider?, direction: Direction, width: Float, tickDelta: Float, time: Long) {
        val matrix = matrices.peek().positionMatrix
        
        var xx1 = x1
        var xx2 = x2
        var yy1 = x1
        var yy2 = x2

        if (x1 < x2) {
            xx1 = x2
            xx2 = x1
        }

        if (y1 < y2) {
            yy1 = y2
            yy2 = y1
        }

        val a = (color shr 24 and 255) / 255.0f
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f

        val vec = direction.unitVector
        var maxU = sprite.getFrameU(4.0)
        var minU = sprite.getFrameU(floor(width.toDouble() * 16))

        val normal = matrices.peek().normalMatrix
        vertexConsumers?.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))?.run {

            fun vertex(x: Float, y: Float, u: Float, v: Float, alpha: Float = a) {
                this@run.vertex(matrix, x, y, 0.0f).color(r, g, b, alpha).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            }

            vertex(xx1, yy1, minU, sprite.minV)
            vertex(xx1, yy2, minU, sprite.maxV)
            vertex(xx2, yy2, maxU, sprite.maxV)
            vertex(xx1, yy1, minU, sprite.minV)
            vertex(xx1, yy1, minU, sprite.minV)
            vertex(xx2, yy2, maxU, sprite.maxV)
            vertex(xx2, yy1, maxU, sprite.minV)
            vertex(xx1, yy1, minU, sprite.minV)

            if (width < 0.75 && width > 0.25) {
                xx2 = xx1
                xx1 = (floor(width * 16) + 1) / 16f
                val opacity = sin((time + tickDelta) / 8).absoluteValue
                maxU = minU
                minU = sprite.getFrameU(floor(width.toDouble() * 16) + 1)
                vertex(xx1, yy1, minU, sprite.minV, opacity)
                vertex(xx1, yy2, minU, sprite.maxV, opacity)
                vertex(xx2, yy2, maxU, sprite.maxV, opacity)
                vertex(xx1, yy1, minU, sprite.minV, opacity)
                vertex(xx1, yy1, minU, sprite.minV, opacity)
                vertex(xx2, yy2, maxU, sprite.maxV, opacity)
                vertex(xx2, yy1, maxU, sprite.minV, opacity)
                vertex(xx1, yy1, minU, sprite.minV, opacity)
            }
        }
    }

    companion object {
        private const val EMISSIVE_LIGHT = 15728880
    }
}
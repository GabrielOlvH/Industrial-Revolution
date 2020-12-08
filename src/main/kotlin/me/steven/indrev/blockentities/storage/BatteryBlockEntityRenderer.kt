package me.steven.indrev.blockentities.storage

import me.steven.indrev.utils.Tier
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.Direction
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.sin

class BatteryBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<BatteryBlockEntity>(dispatcher) {
    override fun render(
        entity: BatteryBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        entity ?: return
        val width = ((entity.energy.toFloat() / entity.maxStoredPower.toFloat()) * 0.5f) + 0.25f
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
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.EAST, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.SOUTH, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.WEST, width, tickDelta, time)
            translate(0.0, 0.0, offset)
            pop()
        }
    }

    private fun drawOverlay(matrices: MatrixStack, x1: Float, y1: Float, x2: Float, y2: Float, color: Long, sprite: Sprite, vertexConsumers: VertexConsumerProvider?, direction: Direction, width: Float, tickDelta: Float, time: Long) {
        val matrix = matrices.peek().model

        var j: Float
        var xx1 = x1
        var xx2 = x2
        var yy1 = x1
        var yy2 = x2

        if (x1 < x2) {
            j = x1
            xx1 = x2
            xx2 = j
        }

        if (y1 < y2) {
            j = y1
            yy1 = y2
            yy2 = j
        }

        val a = (color shr 24 and 255) / 255.0f
        val r = (color shr 16 and 255) / 255.0f
        val g = (color shr 8 and 255) / 255.0f
        val b = (color and 255) / 255.0f

        val vec = direction.unitVector
        var maxU = sprite.getFrameU(4.0)
        var minU = sprite.getFrameU(floor((width.toDouble()) * 16))

        val normal = matrices.peek().normal
        vertexConsumers?.getBuffer(RenderLayer.getEntityTranslucent(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))?.run {
            vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, a).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy2, 0.0f).color(r, g, b, a).texture(minU, sprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx2, yy2, 0.0f).color(r, g, b, a).texture(maxU, sprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, a).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, a).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx2, yy2, 0.0f).color(r, g, b, a).texture(maxU, sprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx2, yy1, 0.0f).color(r, g, b, a).texture(maxU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, a).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()

            if (width < 0.75 && width > 0.25) {
                xx2 = xx1
                xx1 = (floor((width * 16)) + 1) / 16f
                val opacity = sin((time + tickDelta) / 8).absoluteValue
                maxU = minU
                minU = sprite.getFrameU(floor(width.toDouble() * 16) + 1)
                vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, opacity).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx1, yy2, 0.0f).color(r, g, b, opacity).texture(minU, sprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx2, yy2, 0.0f).color(r, g, b, opacity).texture(maxU, sprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, opacity).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, opacity).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx2, yy2, 0.0f).color(r, g, b, opacity).texture(maxU, sprite.maxV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx2, yy1, 0.0f).color(r, g, b, opacity).texture(maxU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(
                    EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
                vertex(matrix, xx1, yy1, 0.0f).color(r, g, b, opacity).texture(minU, sprite.minV).overlay(OverlayTexture.DEFAULT_UV).light(EMISSIVE_LIGHT).normal(normal, vec.x, vec.y, vec.z).next()
            }
        }
    }

    companion object {
        const val EMISSIVE_LIGHT = 15728880
    }
}
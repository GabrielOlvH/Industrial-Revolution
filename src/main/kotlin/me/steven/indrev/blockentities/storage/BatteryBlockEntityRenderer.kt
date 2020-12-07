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
import kotlin.math.floor

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
        val height = ((entity.energy.toFloat() / entity.maxStoredPower.toFloat()) * 0.5f) + 0.25f
        val sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(identifier("block/lazuli_flux_container_lf_level"))
        val color: Long = (255 shl 24 or when (entity.tier) {
            Tier.MK1 -> 0xffbb19
            Tier.MK2 -> 0x5d3dff
            Tier.MK3 -> 0xfd47ff
            else -> 0xff4070
        }).toLong()
        val maxY = floor((height * 16)) / 16f
        matrices?.run {
            val offset = 0.001
            push()

            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0f, 0.25f, 1f, maxY, color, sprite, vertexConsumers, Direction.NORTH, height)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0f, 0.25f, 1f, maxY, color, sprite, vertexConsumers, Direction.EAST, height)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0f, 0.25f, 1f, maxY, color, sprite, vertexConsumers, Direction.SOUTH, height)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0f, 0.25f, 1f, maxY, color, sprite, vertexConsumers, Direction.WEST, height)
            translate(0.0, 0.0, offset)
            pop()
        }
    }

    private fun drawOverlay(matrices: MatrixStack, x1: Float, y1: Float, x2: Float, y2: Float, color: Long, sprite: Sprite, vertexConsumers: VertexConsumerProvider?, direction: Direction, height: Float) {
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

        val f1 = (color shr 24 and 255) / 255.0f
        val g1 = (color shr 16 and 255) / 255.0f
        val h1 = (color shr 8 and 255) / 255.0f
        val k1 = (color and 255) / 255.0f

        val vec = direction.unitVector
        val v = sprite.getFrameV(4.0)
        val minV = sprite.getFrameV(floor((height.toDouble()) * 16))

        val normal = matrices.peek().normal
        vertexConsumers?.getBuffer(RenderLayer.getEntityCutout(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE))?.run {
            vertex(matrix, xx1, yy1, 0.0f).color(g1, h1, k1, f1).texture(sprite.minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy2, 0.0f).color(g1, h1, k1, f1).texture(sprite.minU, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx2, yy2, 0.0f).color(g1, h1, k1, f1).texture(sprite.maxU, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy1, 0.0f).color(g1, h1, k1, f1).texture(sprite.minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy1, 0.0f).color(g1, h1, k1, f1).texture(sprite.minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx2, yy2, 0.0f).color(g1, h1, k1, f1).texture(sprite.maxU, v).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx2, yy1, 0.0f).color(g1, h1, k1, f1).texture(sprite.maxU, minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
            vertex(matrix, xx1, yy1, 0.0f).color(g1, h1, k1, f1).texture(sprite.minU, minV).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(normal, vec.x, vec.y, vec.z).next()
        }
    }
}
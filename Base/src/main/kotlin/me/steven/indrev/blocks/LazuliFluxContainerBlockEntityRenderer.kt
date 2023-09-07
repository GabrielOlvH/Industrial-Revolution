package me.steven.indrev.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.api.blockSpriteId
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.RotationAxis
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.math.sin

class LazuliFluxContainerBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) : MachineBlockEntityRenderer(ctx) {

    override fun render(
        entity: MachineBlockEntity<*>,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (model == null) {
            model = ctx.renderManager.getModel(entity.cachedState) as? MachineBakedModel ?: return
        }

        if (entity !is LazuliFluxContainerBlockEntity) return;

        val vc = vertexConsumers.getBuffer(RenderLayer.getCutout())
        val mutable = BlockPos.Mutable()

        getQuads(entity.tier)?.forEach { quad ->
            mutable.set(entity.pos, quad.face)
            val faceLight = WorldRenderer.getLightmapCoordinates(entity.world, entity.cachedState, mutable)
            vc.quad(matrices.peek(), quad, 1f, 1f, 1f, faceLight, overlay)
        }

        Direction.values().forEach { dir ->
            val mode = entity.sideConfig.getMode(dir)
            val quads = if (mode.allowInput) getInputQuads() else if (mode.allowOutput) getOutputQuads() else return@forEach
            quads.forEach { quad ->
                if (quad.face == dir) {
                    mutable.set(entity.pos, quad.face)
                    val faceLight = WorldRenderer.getLightmapCoordinates(entity.world, entity.cachedState, mutable)
                    vc.quad(matrices.peek(), quad, 1f, 1f, 1f, faceLight, overlay)
                }
            }
        }
        val width = ((entity.energy.toFloat() / entity.capacity.toFloat()) * 0.5f) + 0.25f
        val sprite = MinecraftClient.getInstance().getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply(identifier("block/lazuli_flux_container_lf_level"))
        val color: Long = (255 shl 24 or entity.tier.color).toLong()
        val maxX = floor((width * 16)) / 16f
        val time = entity.world!!.time
        matrices.run {
            val offset = 0.001
            push()
            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.NORTH, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f))
            translate(-0.5, -0.5, -0.5)
            translate(0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.EAST, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f))
            translate(-0.5, -0.5, -0.5)
            translate(-0.0, 0.0, -offset)
            drawOverlay(this, 0.25f, 0f, maxX, 1f, color, sprite, vertexConsumers, Direction.SOUTH, width, tickDelta, time)
            translate(0.0, 0.0, offset)

            translate(0.5, 0.5, 0.5)
            multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f))
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

        private val LFC_TIER_QUADS = mutableMapOf<Tier, List<BakedQuad>>()
        private val INPUT_QUADS = mutableListOf<BakedQuad>()
        private val OUTPUT_QUADS = mutableListOf<BakedQuad>()
        val LFC_TIER_MESHES = mutableMapOf<Tier, Mesh>()

        fun getMesh(tier: Tier): Mesh? {
            if (LFC_TIER_MESHES.contains(tier)) return LFC_TIER_MESHES[tier]
            getQuads(tier)
            return LFC_TIER_MESHES[tier]
        }

        fun getQuads(tier: Tier): List<BakedQuad>? {
            if (LFC_TIER_QUADS.contains(tier)) return LFC_TIER_QUADS[tier]

            val renderer = RendererAccess.INSTANCE.renderer!!
            val builder = renderer.meshBuilder()
            val emitter = builder.emitter
            val quads = mutableListOf<BakedQuad>()
            val sprite =  blockSpriteId("block/lazuli_flux_container_${tier.asString}_overlay").sprite
            Direction.values().forEach { side ->
                emitter.square(side, 0f, 0f, 1f, 1f, -2e-4f * 2)
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
                emitter.spriteColor(0, -1, -1, -1, -1)
                emitter.sprite(0, 0, sprite.minU, sprite.minV)
                emitter.sprite(1, 0, sprite.minU, sprite.maxV)
                emitter.sprite(2, 0, sprite.maxU, sprite.maxV)
                emitter.sprite(3, 0, sprite.maxU, sprite.minV)
                quads.add(emitter.toBakedQuad(0, sprite, false))
                emitter.emit()
            }
            LFC_TIER_QUADS[tier] = quads
            LFC_TIER_MESHES[tier] = builder.build()
            return quads
        }

        fun getInputQuads(): List<BakedQuad> {
            if (INPUT_QUADS.isNotEmpty()) return INPUT_QUADS

            val renderer = RendererAccess.INSTANCE.renderer!!
            val builder = renderer.meshBuilder()
            val emitter = builder.emitter
            val quads = mutableListOf<BakedQuad>()
            val sprite = blockSpriteId("block/lazuli_flux_container_input").sprite
            Direction.values().forEach { side ->
                emitter.square(side, 0f, 0f, 1f, 1f, -2e-4f * 2)
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
                emitter.spriteColor(0, -1, -1, -1, -1)
                emitter.sprite(0, 0, sprite.minU, sprite.minV)
                emitter.sprite(1, 0, sprite.minU, sprite.maxV)
                emitter.sprite(2, 0, sprite.maxU, sprite.maxV)
                emitter.sprite(3, 0, sprite.maxU, sprite.minV)
                quads.add(emitter.toBakedQuad(0, sprite, false))
                emitter.emit()
            }

            INPUT_QUADS.addAll(quads)
            return quads
        }

        fun getOutputQuads(): List<BakedQuad> {
            if (OUTPUT_QUADS.isNotEmpty()) return OUTPUT_QUADS

            val renderer = RendererAccess.INSTANCE.renderer!!
            val builder = renderer.meshBuilder()
            val emitter = builder.emitter
            val quads = mutableListOf<BakedQuad>()
            val sprite = blockSpriteId("block/lazuli_flux_container_output").sprite
            Direction.values().forEach { side ->
                emitter.square(side, 0f, 0f, 1f, 1f, -2e-4f * 2)
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
                emitter.spriteColor(0, -1, -1, -1, -1)
                emitter.sprite(0, 0, sprite.minU, sprite.minV)
                emitter.sprite(1, 0, sprite.minU, sprite.maxV)
                emitter.sprite(2, 0, sprite.maxU, sprite.maxV)
                emitter.sprite(3, 0, sprite.maxU, sprite.minV)
                quads.add(emitter.toBakedQuad(0, sprite, false))
                emitter.emit()
            }

            OUTPUT_QUADS.addAll(quads)
            return quads
        }
    }
}
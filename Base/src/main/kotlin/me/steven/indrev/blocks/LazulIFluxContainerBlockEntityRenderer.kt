package me.steven.indrev.blocks

import me.steven.indrev.api.Tier
import me.steven.indrev.api.blockSpriteId
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.storage.LazuliFluxContainer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction

class LazulIFluxContainerBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) : MachineBlockEntityRenderer(ctx) {

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

        if (entity !is LazuliFluxContainer) return;


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
    }

    companion object {
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
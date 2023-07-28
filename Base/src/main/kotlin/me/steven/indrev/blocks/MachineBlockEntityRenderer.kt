package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.api.Tier
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
import net.minecraft.util.math.RotationAxis

open class MachineBlockEntityRenderer(val ctx: BlockEntityRendererFactory.Context) : BlockEntityRenderer<MachineBlockEntity<*>> {
    var model: MachineBakedModel? = null

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

        val vc = vertexConsumers.getBuffer(RenderLayer.getCutout())
        val mutable = BlockPos.Mutable()
        val facing = entity.cachedState[MachineBlock.FACING]

        if (model?.hasOnModel == true && !entity.idle) {
            if (facing != Direction.NORTH) {
                matrices.push()
                matrices.translate(0.5, 0.5, 0.5)
                matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.opposite.asRotation()))
                matrices.translate(-0.5, -0.5, -0.5)
            }

            model?.onQuads?.forEach { quad ->
                val faceLight =
                    if (model!!.emissives.contains(quad.sprite))
                        15728880
                    else {
                        mutable.set(entity.pos, rotate(quad.face, facing))
                        WorldRenderer.getLightmapCoordinates(entity.world, entity.cachedState, mutable)
                    }
                vc.quad(matrices.peek(), quad, 1f, 1f, 1f, faceLight, overlay)
            }

            if (facing != Direction.NORTH) {
                matrices.pop()
            }
        }

        getQuads(entity.tier)?.forEach { quad ->
            mutable.set(entity.pos, quad.face)
            val faceLight = WorldRenderer.getLightmapCoordinates(entity.world, entity.cachedState, mutable)
            vc.quad(matrices.peek(), quad, 1f, 1f, 1f, faceLight, overlay)
        }


    }

    companion object {
        private val TIER_QUADS = mutableMapOf<Tier, List<BakedQuad>>()
        val TIER_MESHES = mutableMapOf<Tier, Mesh>()

        private val LAZULI_FLUX_TIER_QUADS = mutableMapOf<Tier, List<BakedQuad>>()
        val LAZULI_FLUX_TIER_MESHES = mutableMapOf<Tier, Mesh>()

        fun rotate(face: Direction, dir: Direction): Direction {
            if (face.axis.isVertical) return face
            return when (dir) {
                Direction.SOUTH -> face.opposite
                Direction.WEST -> face.rotateYCounterclockwise()
                Direction.EAST -> face.rotateYClockwise()
                else -> face
            }
        }

        fun getMesh(tier: Tier): Mesh? {
            if (TIER_MESHES.contains(tier)) return TIER_MESHES[tier]
            getQuads(tier)
            return TIER_MESHES[tier]
        }

        fun getQuads(tier: Tier): List<BakedQuad>? {
            if (TIER_QUADS.contains(tier)) return TIER_QUADS[tier]

            val renderer = RendererAccess.INSTANCE.renderer!!
            val builder = renderer.meshBuilder()
            val emitter = builder.emitter
            val quads = mutableListOf<BakedQuad>()
            val sprite = tier.getOverlaySprite().sprite
            Direction.values().forEach { side ->
                emitter.square(side, 0f, 0f, 1f, 1f, -2e-4f * 2)
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
                emitter.spriteColor(0, -1, -1, -1, -1)
                val uv = MachineBakedModel.MachineTextureUV.BY_DIRECTION[side]!!
                emitter.sprite(0, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
                emitter.sprite(1, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
                emitter.sprite(2, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
                emitter.sprite(3, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
                quads.add(emitter.toBakedQuad(0, sprite, false))
                emitter.emit()
            }
            TIER_QUADS[tier] = quads
            TIER_MESHES[tier] = builder.build()
            return quads
        }
    }
}
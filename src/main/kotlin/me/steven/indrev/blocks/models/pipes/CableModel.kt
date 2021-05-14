package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.blockSpriteId
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.minecraft.client.render.model.BakedModel

class CableModel(tier: Tier) : BasePipeModel(tier, "cable") {
    override val spriteIdCollection = mutableListOf(
        blockSpriteId("block/cable_center"),
        blockSpriteId("block/cable_center_emissive_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/cable_wrap"),
        blockSpriteId("block/cable_wire_emissive_${tier.toString().toLowerCase()}")
    )

    override fun buildDefaultMesh(index: Int, model: BakedModel): Mesh {
        if (index == 0) {
            val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
            val builder: MeshBuilder = renderer.meshBuilder()
            val emitter = builder.emitter

            val sprite = spriteArray[1]!!
            model.getQuads(null, null, null).forEach { q ->
                emitter.fromVanilla(q, null, null)
                emitter.emit()
                emitter.fromVanilla(q, null, null)
                emitter.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
                emitter.sprite(0, 0, sprite.getFrameU(3.0), sprite.getFrameV(3.0))
                emitter.sprite(1, 0, sprite.getFrameU(3.0), sprite.getFrameV(13.0))
                emitter.sprite(2, 0, sprite.getFrameU(13.0), sprite.getFrameV(13.0))
                emitter.sprite(3, 0, sprite.getFrameU(13.0), sprite.getFrameV(3.0))
                emitter.material(MATERIAL)
                emitter.emit()
            }
            return builder.build()
        }
        return super.buildDefaultMesh(index, model)
    }

    companion object {
        val MATERIAL by lazy {
            RendererAccess.INSTANCE.renderer?.materialFinder()!!.clear()
                .spriteDepth(1)
                .blendMode(0, BlendMode.TRANSLUCENT)
                .disableAo(0, true)
                .disableDiffuse(0, true)
                .emissive(0, true)
                ?.find()
        }
    }
}
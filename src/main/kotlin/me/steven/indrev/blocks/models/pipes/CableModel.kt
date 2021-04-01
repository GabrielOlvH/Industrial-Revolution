package me.steven.indrev.blocks.models.pipes

import me.steven.indrev.api.machines.Tier
import me.steven.indrev.utils.blockSpriteId
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

class CableModel(tier: Tier) : BasePipeModel(tier, "cable") {
    override val spriteIdCollection = mutableListOf(
        blockSpriteId("block/cable_center"),
        blockSpriteId("block/cable_center_emissive_${tier.toString().toLowerCase()}"),
        blockSpriteId("block/cable_wrap"),
        blockSpriteId("block/cable_wire_emissive_${tier.toString().toLowerCase()}")
    )

    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        super.emitBlockQuads(world, state, pos, randSupplier, context)
        emitOverlay(context)
    }

    override fun emitItemQuads(stack: ItemStack?, p1: Supplier<Random>, context: RenderContext) {
        super.emitItemQuads(stack, p1, context)
        emitOverlay(context)
    }

    private fun emitOverlay(context: RenderContext) {
        val sprite = spriteArray[1]!!
        context.pushTransform { q ->
            q.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
            q.sprite(0, 0, sprite.getFrameU(3.0), sprite.getFrameV(3.0))
            q.sprite(1, 0, sprite.getFrameU(3.0), sprite.getFrameV(13.0))
            q.sprite(2, 0, sprite.getFrameU(13.0), sprite.getFrameV(13.0))
            q.sprite(3, 0, sprite.getFrameU(13.0), sprite.getFrameV(3.0))
            q.material(MATERIAL)
            true
        }
        context.fallbackConsumer().accept(modelArray[0])
        context.popTransform()
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
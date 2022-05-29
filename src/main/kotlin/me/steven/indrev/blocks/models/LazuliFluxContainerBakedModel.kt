package me.steven.indrev.blocks.models

import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.utils.blockSpriteId
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Supplier

class LazuliFluxContainerBakedModel(id: String) : MachineBakedModel("lazuli_flux_container") {

    init {
        overlayIds.addAll(
            arrayOf(
                blockSpriteId("block/lazuli_flux_container"),
                blockSpriteId("block/lazuli_flux_container_input"),
                blockSpriteId("block/lazuli_flux_container_output"),
                blockSpriteId("block/lazuli_flux_container_item_lf_level"),
                blockSpriteId("block/${id}_overlay")
            )
        )
    }

    private val color: Int = when (id.last()) {
        '1' -> 0xffbb19
        '2' -> 0x5d3dff
        '3' -> 0xfd47ff
        else -> 0xff4070
    }

    override fun buildDefaultMesh() {
        val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
        val builder: MeshBuilder = renderer.meshBuilder()
        val emitter = builder.emitter

        for (direction in Direction.values()) {
            emitter.draw(direction, baseSprite!!, -1)
            emitter.draw(direction, overlays[4]!!, -1)
        }
        defaultMesh = builder.build()
    }

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        super.emitBlockQuads(blockView, state, pos, randomSupplier, ctx)
        val blockEntity = blockView.getBlockEntity(pos) as? LazuliFluxContainerBlockEntity ?: return

        val emitter = ctx.emitter
        blockEntity.transferConfig.forEach { side, mode ->
            if (mode.input) {
                emitter.draw(side, overlays[1]!!)
            } else if (mode.output) {
                emitter.draw(side, overlays[2]!!)
            }
        }
    }

    private fun emitHorizontalQuads(sprite: Sprite, ctx: RenderContext) {
        ctx.emitter.run {
            draw(Direction.NORTH, sprite, 255 shl 24 or color)
            draw(Direction.SOUTH, sprite, 255 shl 24 or color)
            draw(Direction.EAST, sprite, 255 shl 24 or color)
            draw(Direction.WEST, sprite, 255 shl 24 or color)
        }
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        super.emitItemQuads(stack, randomSupplier, ctx)
        emitHorizontalQuads(overlays[3]!!, ctx)
    }
}
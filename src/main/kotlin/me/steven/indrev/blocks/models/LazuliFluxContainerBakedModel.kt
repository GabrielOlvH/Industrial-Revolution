package me.steven.indrev.blocks.models

import me.steven.indrev.blockentities.storage.LazuliFluxContainerBlockEntity
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.utils.blockSpriteId
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.texture.Sprite
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Supplier

class LazuliFluxContainerBakedModel(val id: String) : MachineBakedModel("lazuli_flux_container") {

    val outputOverlay = blockSpriteId("block/lazuli_flux_container_output")
    val inputOverlay = blockSpriteId("block/lazuli_flux_container_input")

    init {
        overlayIds.addAll(
            arrayOf(
                blockSpriteId("block/lazuli_flux_container"),
                inputOverlay,
                outputOverlay,
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

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        val blockEntity = blockView.getBlockEntity(pos) as? LazuliFluxContainerBlockEntity ?: return
        val block = state.block as? MachineBlock ?: return
        val direction = block.getFacing(state)

        val emitter = ctx.emitter
        emitQuads(direction, sprite!!, ctx)
        blockEntity.transferConfig.forEach { side, mode ->
            if (mode.input) {
                emitter.draw(direction, side, overlays[1]!!)
            } else if (mode.output) {
                emitter.draw(direction, side, overlays[2]!!)
            }
        }
        emitQuads(direction, overlays[4]!!, ctx)
    }

    private fun emitHorizontalQuads(facing: Direction?, sprite: Sprite, ctx: RenderContext) {
        ctx.emitter.run {
            draw(facing, Direction.NORTH, sprite, 255 shl 24 or color)
            draw(facing, Direction.SOUTH, sprite, 255 shl 24 or color)
            draw(facing, Direction.EAST, sprite, 255 shl 24 or  color)
            draw(facing, Direction.WEST, sprite,255 shl 24 or  color)
        }
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        emitQuads(null, sprite!!, ctx)
        emitQuads(null, overlays[4]!!, ctx)
        emitHorizontalQuads(null, overlays[3]!!, ctx)
    }

    private fun handleBakedModel(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext,
        bakedModel: BakedModel?) {
        if (bakedModel is FabricBakedModel) bakedModel.emitBlockQuads(world, state, pos, randSupplier, context)
        else if (bakedModel != null) context.fallbackConsumer().accept(bakedModel)
    }
}
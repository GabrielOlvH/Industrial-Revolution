package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.BlockRotation

class MultiblockBlockEntityRenderer<T : BlockEntity>(val provider: (T) -> MultiBlockComponent) : BlockEntityRenderer<T> {
    override fun render(
        entity: T,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val multiblock = provider(entity)
        if (!multiblock.shouldRenderHologram) return
        val rotation = MultiblockMatcher.rotateBlock(entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        val def = multiblock.getSelectedMatcher(entity.world!!, entity.pos, entity.cachedState).definition
        def.holder.variants.values.toList()[multiblock.variant % def.holder.variants.size].forEach { (offset, state) ->
            matrices.push()
            val rotated = offset.rotate(rotation)
            val blockPos = entity.pos.subtract(rotated)
            val blockState = entity.world!!.getBlockState(blockPos)
            if (blockState.material.isReplaceable) {
                matrices.translate(-rotated.x.toDouble() + 0.25, -rotated.y.toDouble() + 0.25, -rotated.z.toDouble() + 0.25)
                matrices.scale(0.5f, 0.5f, 0.5f)
                MinecraftClient.getInstance().blockRenderManager
                    .renderBlockAsEntity(state.display.rotate(rotation.rotate(BlockRotation.CLOCKWISE_180)), matrices, vertexConsumers, 15728880, overlay)
            }
            matrices.pop()
        }
    }

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true
}
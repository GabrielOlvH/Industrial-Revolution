package me.steven.indrev.components.multiblock

import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

class MultiblockBlockEntityRenderer<T : BlockEntity>(val multiblock: (T) -> MultiBlockComponent) : BlockEntityRenderer<T> {
    override fun render(
        entity: T,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        multiblock(entity).render(entity, matrices, vertexConsumers, overlay)
    }

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true
}
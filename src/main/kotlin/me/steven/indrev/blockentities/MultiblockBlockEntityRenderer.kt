package me.steven.indrev.blockentities

import me.steven.indrev.components.ComponentKey
import me.steven.indrev.components.ComponentProvider
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

open class MultiblockBlockEntityRenderer<T : BlockEntity>(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<T>(dispatcher) {
    override fun render(
        entity: T,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        ComponentKey.MULTIBLOCK.get(entity as? ComponentProvider ?: return)
            ?.render(entity, matrices, vertexConsumers, overlay)
    }

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true
}
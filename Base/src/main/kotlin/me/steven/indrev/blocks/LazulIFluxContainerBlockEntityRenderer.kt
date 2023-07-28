package me.steven.indrev.blocks

import me.steven.indrev.blockentities.MachineBlockEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos

class LazulIFluxContainerBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) : MachineBlockEntityRenderer(ctx) {

    override fun render(
        entity: MachineBlockEntity<*>,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay)
    }
}
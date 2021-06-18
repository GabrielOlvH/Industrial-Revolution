package me.steven.indrev.blockentities.farms

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.util.math.MatrixStack

class ChopperBlockEntityRenderer : AOEMachineBlockEntityRenderer() {
    override fun render(
        blockEntity: AOEMachineBlockEntity<*>,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        super.render(blockEntity, tickDelta, matrices, vertexConsumers, light, overlay)
        if (blockEntity.renderWorkingArea) {
            val pos = blockEntity.pos
            val area = blockEntity.getWorkingArea().offset(-pos.x.toDouble(), -pos.y.toDouble(), -pos.z.toDouble()).expand(4.0, 0.0, 4.0)
            val vc = vertexConsumers.getBuffer(RenderLayer.getLines())
            WorldRenderer.drawBox(matrices, vc, area, 1f, 0.2f, 0.2f, 1f)
        }

    }
}
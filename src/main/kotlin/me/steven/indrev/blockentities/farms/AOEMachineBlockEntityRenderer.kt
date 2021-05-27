package me.steven.indrev.blockentities.farms

import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

open class AOEMachineBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<AOEMachineBlockEntity<*>>(dispatcher) {
    override fun render(
        blockEntity: AOEMachineBlockEntity<*>,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (blockEntity.renderWorkingArea) {
            val pos = blockEntity.pos
            val area = blockEntity.getWorkingArea().offset(-pos.x.toDouble(), -pos.y.toDouble(), -pos.z.toDouble())
            val vc = vertexConsumers.getBuffer(RenderLayer.getLines())
            WorldRenderer.drawBox(matrices, vc, area,  1f, 0f, 1f, 1f)
        }
    }

    override fun rendersOutsideBoundingBox(blockEntity: AOEMachineBlockEntity<*>?): Boolean = true

}
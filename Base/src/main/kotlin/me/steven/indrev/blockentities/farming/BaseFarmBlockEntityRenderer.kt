package me.steven.indrev.blockentities.farming

import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.MachineBlockEntityRenderer
import me.steven.indrev.utils.rgb
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack

class BaseFarmBlockEntityRenderer(ctx: BlockEntityRendererFactory.Context) : MachineBlockEntityRenderer(ctx) {
    override fun render(
        entity: MachineBlockEntity<*>,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (entity is BaseFarmBlockEntity<*> && entity.getRange() > 0 && entity.renderWorkingArea) {
            matrices.push()
            matrices.translate(-entity.pos.x.toDouble(), -entity.pos.y.toDouble(), -entity.pos.z.toDouble())
            val (r, g, b) = rgb(entity.getRenderColor())
            WorldRenderer.drawBox(matrices, vertexConsumers.getBuffer(RenderLayer.getLines()), entity.getArea(), r / 255f, g / 255f, b / 255f, 1f)
            matrices.pop()
        }
        super.render(entity, tickDelta, matrices, vertexConsumers, light, overlay)
    }

    override fun rendersOutsideBoundingBox(blockEntity: MachineBlockEntity<*>): Boolean {
        return true
    }
}
package me.steven.indrev.blockentities.farms

import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import kotlin.math.floor

class PumpBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<PumpBlockEntity>(dispatcher) {
    override fun render(
        entity: PumpBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val state = Blocks.TORCH.defaultState
        val currentY = floor(entity.movingTicks).toInt()
        for (y in 1..currentY) {
            matrices.push()
            matrices.translate(0.0, -y.toDouble(), 0.0)
            MinecraftClient.getInstance().blockRenderManager.renderBlock(
                state,
                entity.pos,
                entity.world,
                matrices,
                vertexConsumers.getBuffer(RenderLayers.getBlockLayer(state)),
                false,
                entity.world!!.random
            )
            matrices.pop()
        }
        if (currentY.toDouble() != entity.movingTicks) {
            matrices.push()
            matrices.translate(0.0, -entity.movingTicks, 0.0)
            MinecraftClient.getInstance().blockRenderManager.renderBlock(
                state,
                entity.pos,
                entity.world,
                matrices,
                vertexConsumers.getBuffer(RenderLayers.getBlockLayer(state)),
                false,
                entity.world!!.random
            )
            matrices.pop()
        }
    }
}
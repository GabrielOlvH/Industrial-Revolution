package me.steven.indrev.blocks.cables

import net.minecraft.block.Blocks
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.registry.Registry

class CableBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<CableBlockEntity>(dispatcher) {
    override fun render(blockEntity: CableBlockEntity?, tickDelta: Float, matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, light: Int, overlay: Int) {
        if (blockEntity?.cover != null) {
            val state = Registry.BLOCK.get(blockEntity.cover).defaultState
            if (state == Blocks.AIR.defaultState) return
            matrices?.push()
            val vertexConsumer = vertexConsumers?.getBuffer(RenderLayers.getBlockLayer(state)) ?: return
            MinecraftClient.getInstance().blockRenderManager.renderBlock(state, blockEntity.pos, blockEntity.world, matrices, vertexConsumer, true, blockEntity.world?.random)
            matrices?.pop()
        }
    }
}
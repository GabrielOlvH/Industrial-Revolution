package me.steven.indrev.blockentities.farms

import me.steven.indrev.registry.ModRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

class AOEMachineBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<AOEMachineBlockEntity>(dispatcher) {
    override fun render(
        blockEntity: AOEMachineBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        if (blockEntity.renderWorkingArea) {
            val area = blockEntity.getWorkingArea()
            val state = ModRegistry.AREA_INDICATOR.defaultState
            matrices?.push()
            val vertexConsumer = vertexConsumers?.getBuffer(RenderLayers.getBlockLayer(state)) ?: return
            val pos = blockEntity.pos
            for (x in area.x1.toInt() until area.x2.toInt())
                for (y in area.y1.toInt() until area.y2.toInt())
                    for (z in area.z1.toInt() until area.z2.toInt()) {
                        val offsetX = pos.x - x.toDouble()
                        val offsetY = y.toDouble() - pos.y
                        val offsetZ = pos.z - z.toDouble()
                        matrices?.translate(offsetX, offsetY, offsetZ)
                        MinecraftClient.getInstance().blockRenderManager.renderBlock(
                            state,
                            pos,
                            blockEntity.world,
                            matrices,
                            vertexConsumer,
                            true,
                            blockEntity.world?.random
                        )
                        matrices?.translate(-offsetX, -offsetY, -offsetZ)
                    }
            matrices?.pop()
        }
    }

}
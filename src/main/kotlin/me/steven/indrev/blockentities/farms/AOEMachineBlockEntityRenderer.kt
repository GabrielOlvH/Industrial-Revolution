package me.steven.indrev.blockentities.farms

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Box


class AOEMachineBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<AOEMachineBlockEntity<*>>(dispatcher) {
    override fun render(
        blockEntity: AOEMachineBlockEntity<*>,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        if (blockEntity.renderWorkingArea) {
            val area = blockEntity.getWorkingArea()

            renderBox(blockEntity, matrices, vertexConsumers, area, 1f, 0f, 1f)

            if (blockEntity is ChopperBlockEntity) {
                // Render extra box to show breaking range
                renderBox(blockEntity, matrices, vertexConsumers, area.expand(4.0, 0.0, 4.0), 1f, 0f, 0f)
            }
        }
    }

    fun renderBox (blockEntity: AOEMachineBlockEntity<*>, matrices: MatrixStack?, vertexConsumers: VertexConsumerProvider?, area: Box, red: Float, green: Float, blue: Float) {
        RenderSystem.enableBlend()
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO)
        RenderSystem.lineWidth((MinecraftClient.getInstance().window.framebufferHeight.toFloat() / 1920.0f * 2.5f).coerceAtLeast(2.5f))
        RenderSystem.disableTexture()
        val pos = blockEntity.pos
        matrices?.run {
            push()
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(515)
            RenderSystem.depthMask(true)
            val vertex = vertexConsumers?.getBuffer(RenderLayer.getLines())
            WorldRenderer.drawBox(this, vertex, area.offset(-pos.x.toDouble(), -pos.y.toDouble(), -pos.z.toDouble()), red, green, blue, 1f)
            pop()
        }
        RenderSystem.enableTexture()
        RenderSystem.disableBlend()
    }

    override fun rendersOutsideBoundingBox(blockEntity: AOEMachineBlockEntity<*>?): Boolean = true

}
package me.steven.indrev.blockentities.farms

import me.steven.indrev.utils.component1
import me.steven.indrev.utils.component2
import me.steven.indrev.utils.component3
import me.steven.indrev.utils.toVec3d
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier

class PumpBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<PumpBlockEntity>(dispatcher) {
    override fun render(
        entity: PumpBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        MinecraftClient.getInstance().textureManager.bindTexture(PIPE_TEXTURE)
        val (x, y, z) = entity.pos.down().toVec3d()
        matrices.push()
        matrices.translate(x, y, z)
        Tessellator.getInstance().buffer.run {
            begin(7, VertexFormats.POSITION_COLOR)
            WorldRenderer.drawBox(this, 0.2, 0.0, 0.2, 0.7, y - entity.movingTicks, 0.7, 0.5f, 0.5f, 0.5f, 1f)
            Tessellator.getInstance().draw()
        }
        matrices.pop()
    }

    companion object {
        val PIPE_TEXTURE = Identifier("minecraft:textures/block/iron_block.png")
    }
}
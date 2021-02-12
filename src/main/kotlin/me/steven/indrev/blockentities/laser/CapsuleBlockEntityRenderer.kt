package me.steven.indrev.blockentities.laser

import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f

class CapsuleBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<CapsuleBlockEntity>(dispatcher) {

    override fun render(
        entity: CapsuleBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        entity ?: return
        val itemStack = entity.inventory[0]
        if (itemStack.isEmpty) return
        val lightCoord = WorldRenderer.getLightmapCoordinates(entity.world, entity.pos)
        matrices?.run {
            push()
            val time = entity.world?.time ?: 1
            translate(0.5, 0.35, 0.5)
            scale(1.2f, 1.2f, 1.2f)
            multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((time + tickDelta) * 16 * entity.getActiveLasersCount()))
            MinecraftClient.getInstance().itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GROUND, lightCoord, overlay, matrices, vertexConsumers)
            pop()
        }
    }
}
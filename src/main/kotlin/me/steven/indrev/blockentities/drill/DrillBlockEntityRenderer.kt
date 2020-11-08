package me.steven.indrev.blockentities.drill

import me.steven.indrev.blocks.machine.DrillBlock
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayers
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f

class DrillBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<DrillBlockEntity>(dispatcher) {
    override fun render(
        entity: DrillBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (entity.cachedState[DrillBlock.WORKING]) {
            val model =
                MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(identifier("drill_head"), ""))
            matrices?.run {
                push()
                val entry = peek()
                translate(0.5, 0.0, 0.5)
                multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((entity.world!!.time + tickDelta) * 4))
                translate(-0.5, 0.0, -0.5)
                MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
                    entry,
                    vertexConsumers.getBuffer(RenderLayers.getBlockLayer(entity.cachedState)),
                    null,
                    model,
                    -1f,
                    -1f,
                    -1f,
                    WorldRenderer.getLightmapCoordinates(entity.world, entity.pos),
                    OverlayTexture.DEFAULT_UV
                )
                pop()
            }
        }

    }

    override fun rendersOutsideBoundingBox(blockEntity: DrillBlockEntity?): Boolean = true
}
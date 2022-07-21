package me.steven.indrev.blockentities.farms

import me.steven.indrev.blocks.misc.BiomassComposterBlock
import me.steven.indrev.utils.bucket
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack

class BiomassComposterBlockEntityRenderer : BlockEntityRenderer<BiomassComposterBlockEntity> {
    override fun render(
        entity: BiomassComposterBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        if (entity.cachedState[BiomassComposterBlock.CLOSED]) return
        matrices.run {

            if (entity.level > 0) {
                push()
                translate(0.0, ((((entity.level - 1) * 2)) / 16.0), 0.0)

                val model = MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(identifier("composting"), ""))
                MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
                    matrices.peek(),
                    vertexConsumers.getBuffer(RenderLayer.getTranslucent()),
                    null,
                    model,
                    -1f,
                    -1f,
                    -1f,
                    WorldRenderer.getLightmapCoordinates(entity.world, entity.pos.up()),
                    OverlayTexture.DEFAULT_UV
                )
                pop()
            }
            push()
            val vol = entity.fluidInv.amount / bucket.toDouble()
            translate(0.0, vol, 0.0)
            //entity.fluidInv.render(FACE, vertexConsumers, matrices)
            pop()
        }
    }

    companion object {
        //private val FACE = listOf(FluidRenderFace.createFlatFaceY(0.125, 0.0, 0.125, 0.875, 0.0, 0.875, 1.0, true, false))
    }
}
package me.steven.indrev.blockentities.farms

import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.utils.IRFluidTank
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f
import kotlin.math.floor

class PumpBlockEntityRenderer : BlockEntityRenderer<PumpBlockEntity> {
    override fun render(
        entity: PumpBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        matrices.run {

            push()
            val inputVolume = entity.fluidComponent!![0]
            if (!inputVolume.isEmpty) {

                translate(0.5, 0.5, 0.5)
                var direction = entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
                if (direction.axis == Direction.Axis.X) direction = direction.opposite
                multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(direction.asRotation()))
                translate(-0.5, -0.5, -0.5)
                push()
                multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(22.5f))
                renderFluid(inputVolume, vertexConsumers)
                pop()

                push()
                multiply(Vec3f.NEGATIVE_Z.getDegreesQuaternion(22.5f))
                translate(0.5, 0.5, 0.5)
                multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(90f))
                translate(-0.5, -0.5, -0.5)
                matrices.translate(0.0, 0.38, 0.0765)
                renderFluid(inputVolume, vertexConsumers)
                pop()

                push()
                multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(22.5f))
                translate(0.5, 0.5, 0.5)
                multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(180f))
                translate(-0.5, -0.5, -0.5)
                matrices.translate(0.1284, 0.0, 0.1285)
                renderFluid(inputVolume, vertexConsumers)
                pop()
            }
            pop()
            val currentY = floor(entity.movingTicks).toInt()
            for (y in 1..currentY) {
                push()
                translate(0.0, -y.toDouble(), 0.0)
                renderModel(vertexConsumers, entity)
                pop()
            }
            if (currentY.toDouble() != entity.movingTicks) {
                push()
                scale(1.01f, 1f, 1.01f)
                translate(-0.005, -entity.movingTicks, -0.005)
                renderModel(vertexConsumers, entity)
                pop()
            }
        }
    }

    private fun MatrixStack.renderModel(vertexConsumers: VertexConsumerProvider, entity: PumpBlockEntity) {
        val model = MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(identifier("pump_pipe"), ""))
        val buffer = vertexConsumers.getBuffer(RenderLayer.getSolid())
        val light = WorldRenderer.getLightmapCoordinates(entity.world, entity.pos)
        MinecraftClient.getInstance().blockRenderManager.modelRenderer.render(
            peek(), buffer, null, model, -1f, -1f, -1f, light, OverlayTexture.DEFAULT_UV
        )
    }

    private fun MatrixStack.renderFluid(inputVolume: IRFluidTank, vcp: VertexConsumerProvider) {

        inputVolume.render(FACES, vcp, this)
    }

    override fun rendersOutsideBoundingBox(blockEntity: PumpBlockEntity?): Boolean = true

    companion object {
        val FACES = listOf(
            FluidRenderFace.createFlatFaceZ(0.443, 0.15, 0.32, 0.556, 0.31, 0.32, 2.0, false, false),
            FluidRenderFace.createFlatFaceZ(0.443, 0.15, 0.32+ (0.55-0.443), 0.556, 0.31, 0.32+ (0.55-0.443), 2.0, true, false),
            FluidRenderFace.createFlatFaceX(0.443, 0.15, 0.323, 0.443, 0.31, 0.426, 2.0, false, false),
            FluidRenderFace.createFlatFaceX(0.443+ (0.55-0.443), 0.15, 0.323, 0.443+ (0.55-0.443), 0.31, 0.426, 2.0, true, false)
        )
    }
}
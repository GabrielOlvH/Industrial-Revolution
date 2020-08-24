package me.steven.indrev.blockentities.crafters

import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer
import me.steven.indrev.blocks.HorizontalFacingMachineBlock
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction

class CondenserBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<CondenserBlockEntity>(dispatcher) {

    override fun render(
        entity: CondenserBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        val fluidComponent = entity?.fluidComponent ?: return
        val faces = when (entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]) {
            Direction.SOUTH ->
                FluidRenderFace.createFlatFaceZ(0.185, 0.625, 1.005, 0.81, 0.815, 1.005, 2.0, true, false)
            Direction.NORTH ->
                FluidRenderFace.createFlatFaceZ(0.815, 0.625, -0.005, 0.19, 0.815, -0.005, 2.0, true, false)
            Direction.WEST ->
                FluidRenderFace.createFlatFaceX(-0.005, 0.625, 0.185, -0.005, 0.815, 0.81, 2.0, false, false)
            Direction.EAST ->
                FluidRenderFace.createFlatFaceX(1.005, 0.625, 0.815, 1.005, 0.815, 0.19, 2.0, false, false)
            else -> return
        }
        fluidComponent.tanks[0].volume.render(listOf(faces), FluidVolumeRenderer.VCPS, matrices)
        FluidVolumeRenderer.VCPS.draw()
    }
}
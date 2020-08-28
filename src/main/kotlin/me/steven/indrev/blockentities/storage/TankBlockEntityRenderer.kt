package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer
import me.steven.indrev.blocks.TankBlock
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

class TankBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<TankBlockEntity>(dispatcher) {
    override fun render(
        entity: TankBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        val fluidComponent = entity.fluidComponent
        val volume = fluidComponent.tanks[0].volume
        if (volume.isEmpty) return
        val fluid = volume.amount().asInt(1000)
        val maxFluid = fluidComponent.limit.asInt(1000).toDouble()
        var percent = fluid.toFloat() / maxFluid.toFloat()
        val maxHeight = if (entity.cachedState[TankBlock.UP]) 16 else 14
        percent = (percent * maxHeight).toInt() / 16f
        val yHeight = percent.toDouble()
        val FACES = listOf(
            FluidRenderFace.createFlatFaceZ(0.9, 0.0, 0.1, 0.1, yHeight, 0.1, 2.0, true, false),
            FluidRenderFace.createFlatFaceZ(0.1, 0.0, 0.9, 0.9, yHeight, 0.9, 2.0, true, false),
            FluidRenderFace.createFlatFaceX(0.1, 0.0, 0.1, 0.1, yHeight, 0.9, 2.0, false, false),
            FluidRenderFace.createFlatFaceX(0.9, 0.0, 0.9, 0.9, yHeight, 0.1, 2.0, false, false),
            FluidRenderFace.createFlatFaceY(0.1, yHeight, 0.1, 0.9, yHeight, 0.9, 2.0, true, false)
        )
        volume.render(FACES, FluidVolumeRenderer.VCPS, matrices)
        FluidVolumeRenderer.VCPS.draw()
    }
}
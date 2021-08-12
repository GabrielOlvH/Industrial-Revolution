package me.steven.indrev.blockentities.storage

import alexiil.mc.lib.attributes.fluid.render.FluidRenderFace
import alexiil.mc.lib.attributes.fluid.render.FluidVolumeRenderer
import me.steven.indrev.blocks.misc.TankBlock
import me.steven.indrev.utils.IRFluidVolumeRenderer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

class TankBlockEntityRenderer : BlockEntityRenderer<TankBlockEntity> {
    override fun render(
        entity: TankBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        val fluidComponent = entity.fluidComponent
        val volume = fluidComponent[0]
        if (volume.isEmpty) return
        val fluid = volume.amount().asInt(1000)
        val maxFluid = fluidComponent.limit.asInt(1000).toDouble()
        var percent = fluid.toFloat() / maxFluid.toFloat()
        val maxHeight = if (entity.cachedState[TankBlock.UP]) 16 else 14
        percent = (percent * maxHeight).toInt() / 16f
        val yHeight = percent.toDouble().coerceAtLeast(0.1)
        val faces = mutableListOf(
            FluidRenderFace.createFlatFaceZ(0.9, 0.0, 0.1, 0.1, yHeight, 0.1, 1.0, true, false),
            FluidRenderFace.createFlatFaceZ(0.1, 0.0, 0.9, 0.9, yHeight, 0.9, 1.0, true, false),
            FluidRenderFace.createFlatFaceX(0.1, 0.0, 0.1, 0.1, yHeight, 0.9, 1.0, false, false),
            FluidRenderFace.createFlatFaceX(0.9, 0.0, 0.9, 0.9, yHeight, 0.1, 1.0, false, false),
        )

        var renderFluidTop = true
        if (entity.cachedState[TankBlock.UP]) {
            val aboveTank = entity.world!!.getBlockEntity(entity.pos.up()) as? TankBlockEntity
            renderFluidTop = aboveTank?.fluidComponent?.get(0)?.fluidKey != volume.fluidKey
        }
        if (renderFluidTop) {
            faces.add(FluidRenderFace.createFlatFaceY(0.1, yHeight, 0.1, 0.9, yHeight, 0.9, 1.0, true, false))
        }

        for (face in faces) {
            face.light = light;
        }

        volume.render(faces, vertexConsumers, matrices);
    }
}

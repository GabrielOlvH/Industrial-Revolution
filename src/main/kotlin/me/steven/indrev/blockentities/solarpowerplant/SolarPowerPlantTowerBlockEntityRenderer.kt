package me.steven.indrev.blockentities.solarpowerplant

import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

class SolarPowerPlantTowerBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<SolarPowerPlantTowerBlockEntity>(dispatcher) {

    override fun render(
        entity: SolarPowerPlantTowerBlockEntity,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        entity.multiblockComponent.render(entity, matrices, vertexConsumers, overlay)
    }

    override fun rendersOutsideBoundingBox(blockEntity: SolarPowerPlantTowerBlockEntity?): Boolean = true
}
package me.steven.indrev.blockentities.laser

import me.steven.indrev.blocks.machine.FacingMachineBlock
import me.steven.indrev.utils.identifier
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

class LaserBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<LaserBlockEntity>(dispatcher) {

    override fun render(
        entity: LaserBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        entity ?: return
        if (!entity.isEmittingLaser()) return
        val direction = entity.cachedState[FacingMachineBlock.FACING]
        matrices?.run {
            push()
            translate(direction.unitVector.x.toDouble(), direction.unitVector.y.toDouble(), direction.unitVector.z.toDouble())
            translate(0.5, 0.5, 0.5)
            val rotation = direction.let {
                when {
                    it.axis.isHorizontal -> it.rotateYCounterclockwise().unitVector.getDegreesQuaternion(90f)
                    it == Direction.DOWN -> Direction.EAST.unitVector.getDegreesQuaternion(180f)
                    else -> it.unitVector.getDegreesQuaternion(90f)
                }
            }
            multiply(rotation)
            translate(-0.5, -0.5, -0.5)
            BeaconBlockEntityRenderer.renderLightBeam(
                matrices,
                vertexConsumers,
                TEXTURE,
                0f,
                1.0f,
                0,
                0,
                3,
                floatArrayOf(1f, 1f, 1f),
                0.08f,
                0.1f
            )
            pop()
        }

    }

    override fun rendersOutsideBoundingBox(blockEntity: LaserBlockEntity?): Boolean = true

    companion object {
        val TEXTURE: Identifier = identifier("textures/entity/laser.png")
    }
}
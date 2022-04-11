package me.steven.indrev.blockentities.miningrig

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3f

class MiningRigBlockEntityRenderer : BlockEntityRenderer<MiningRigBlockEntity> {
    override fun render(
        entity: MiningRigBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        if (entity == null || !entity.workingState) return
        matrices?.run {
            push()
            val direction = entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING]
            when (direction) {
                Direction.NORTH -> translate(0.34, 0.6, -0.01)
                Direction.SOUTH -> translate(0.66, 0.6, 1.01)
                Direction.WEST -> translate(-0.01, 0.6, 0.66)
                Direction.EAST -> translate(1.01, 0.6, 0.34)
                else -> {
                    pop()
                    return
                }
            }
            multiply(Vec3f.NEGATIVE_Y.getDegreesQuaternion(direction.asRotation()))
            scale(0.3f, 0.3f, 0.01f)
            MinecraftClient.getInstance().itemRenderer.renderItem(entity.lastMinedItem, ModelTransformation.Mode.GUI, 15728880, overlay, this, vertexConsumers, 0)
            pop()
        }
    }
}
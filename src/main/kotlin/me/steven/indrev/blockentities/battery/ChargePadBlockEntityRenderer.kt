package me.steven.indrev.blockentities.battery

import me.steven.indrev.blocks.FacingMachineBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.WorldRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.client.util.math.Vector3f
import net.minecraft.util.math.Direction
import kotlin.math.abs
import kotlin.math.sin

class ChargePadBlockEntityRenderer(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<ChargePadBlockEntity>(dispatcher) {
    override fun render(
        entity: ChargePadBlockEntity?,
        tickDelta: Float,
        matrices: MatrixStack?,
        vertexConsumers: VertexConsumerProvider?,
        light: Int,
        overlay: Int
    ) {
        val inventory = entity?.inventoryController?.inventory
        val stack = inventory?.getStack(0)
        if (stack?.isEmpty == false) {
            val facing = entity.cachedState[FacingMachineBlock.HORIZONTAL_FACING]
            var x = abs(facing.offsetX.toFloat()) * 0.5
            var z = abs(facing.offsetZ.toFloat()) * 0.5
            when (facing.rotateYClockwise().axis) {
                Direction.Axis.X -> x += 0.2
                Direction.Axis.Z -> z += 0.2
                else -> return
            }
            matrices?.push()
            val time = entity.world?.time ?: 1
            val offset = sin((time + tickDelta) / 16.0) / 8.0
            matrices?.translate(x, 1.2 + offset, z)
            matrices?.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((time + tickDelta) * 4))
            matrices?.scale(0.5f, 0.5f, 0.5f)
            val lightAbove = WorldRenderer.getLightmapCoordinates(entity.world, entity.pos.up())
            MinecraftClient.getInstance().itemRenderer.renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, overlay, matrices, vertexConsumers)
            matrices?.pop()
        }
    }
}
package me.steven.indrev.blockentities.storage

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
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
        val inventory = entity?.inventoryComponent?.inventory
        val stack = inventory?.getStack(0)
        if (stack?.isEmpty == false) {
            val facing = entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].rotateYClockwise()
            var x = abs(facing.offsetX.toFloat()) * 0.5
            var z = abs(facing.offsetZ.toFloat()) * 0.5
            if (facing.offsetX == -1)
                z = x
            else if (facing.offsetZ == 1) x = z
            when (facing.axis) {
                Direction.Axis.X -> z += 0.2
                Direction.Axis.Z -> x += 0.2
                else -> return
            }
            matrices?.run {
                push()
                val time = entity.world?.time ?: 1
                val offset = sin((time + tickDelta) / 16.0) / 32.0
                translate(x, 1.1 + offset, z)
                multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion((time + tickDelta) * 4))
                scale(0.5f, 0.5f, 0.5f)
                val lightAbove = WorldRenderer.getLightmapCoordinates(entity.world, entity.pos.up())
                MinecraftClient.getInstance().itemRenderer.renderItem(stack, ModelTransformation.Mode.GROUND, lightAbove, overlay, this, vertexConsumers)
                pop()
            }
        }
    }
}
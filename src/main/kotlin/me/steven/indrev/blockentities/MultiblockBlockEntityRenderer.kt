package me.steven.indrev.blockentities

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack

class MultiblockBlockEntityRenderer<T : MachineBlockEntity<*>>(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<T>(dispatcher) {
    override fun render(
        entity: T,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val multiblock = entity.multiblockComponent ?: return
        val rotation = multiblock.rotateBlock(entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        multiblock.structure.forEach { (offset, state) ->
            matrices.push()
            val rotated = offset.rotate(rotation)
            val blockPos = entity.pos.subtract(rotated)
            val blockState = entity.world!!.getBlockState(blockPos)
            if (blockState.isAir) {
                matrices.translate(-rotated.x.toDouble() + 0.25, -rotated.y.toDouble() + 0.25, -rotated.z.toDouble() + 0.25)
                matrices.scale(0.5f, 0.5f, 0.5f)
                MinecraftClient.getInstance().blockRenderManager
                    .renderBlockAsEntity(state, matrices, vertexConsumers, 15728880, overlay)
            } else if (blockState != state) {
                matrices.translate(-rotated.x.toDouble() - 0.00025, -rotated.y.toDouble() - 0.00025, -rotated.z.toDouble() - 0.00025)
                matrices.scale(1.0005f, 1.0005f, 1.0005f)
                MinecraftClient.getInstance().blockRenderManager
                    .renderBlockAsEntity(blockState, matrices, vertexConsumers, 15728880, 2)
            }
            matrices.pop()
        }
    }

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true
}
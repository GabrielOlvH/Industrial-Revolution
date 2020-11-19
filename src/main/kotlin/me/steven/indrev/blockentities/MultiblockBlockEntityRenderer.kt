package me.steven.indrev.blockentities

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import me.steven.indrev.components.multiblock.AbstractMultiblockMatcher
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.BlockRotation

open class MultiblockBlockEntityRenderer<T : MachineBlockEntity<*>>(dispatcher: BlockEntityRenderDispatcher) : BlockEntityRenderer<T>(dispatcher) {
    override fun render(
        entity: T,
        tickDelta: Float,
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        light: Int,
        overlay: Int
    ) {
        val multiblock = entity.multiblockComponent ?: return
        if (!multiblock.shouldRenderHologram) return
        val rotation = AbstractMultiblockMatcher.rotateBlock(entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        multiblock.getSelectedMatcher(entity.world!!, entity.pos, entity.cachedState).definitions.forEach { def ->
            def.structure.forEach { (offset, state) ->
                matrices.push()
                val rotated = offset.rotate(rotation)
                val blockPos = entity.pos.subtract(rotated)
                val blockState = entity.world!!.getBlockState(blockPos)
                if (blockState.isAir) {
                    matrices.translate(-rotated.x.toDouble() + 0.25, -rotated.y.toDouble() + 0.25, -rotated.z.toDouble() + 0.25)
                    matrices.scale(0.5f, 0.5f, 0.5f)
                    MinecraftClient.getInstance().blockRenderManager
                        .renderBlockAsEntity(state.display.rotate(rotation.rotate(BlockRotation.CLOCKWISE_180)), matrices, vertexConsumers, 15728880, overlay)
                }
                matrices.pop()
            }
        }
    }

    override fun rendersOutsideBoundingBox(blockEntity: T): Boolean = true
}
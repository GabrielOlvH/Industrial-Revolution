package me.steven.indrev.components.multiblock

import me.steven.indrev.blocks.machine.HorizontalFacingMachineBlock
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

open class MultiBlockComponent(
    private val isBuilt: (StructureIdentifier) -> Boolean,
    val structureDecider: (BlockState, World, BlockPos) -> StructureDefinition
) {
    var shouldRenderHologram = false
    var variant = 0
    private var ticks = 0
    private var cachedMatchers: MutableMap<String, AbstractMultiblockMatcher> = hashMapOf()

    open fun tick(world: World, pos: BlockPos, blockState: BlockState) {
        ticks++
        if (ticks % 15 != 0) return
        getSelectedMatcher(world, pos, blockState).tick(world, pos, blockState)
    }

    fun getSelectedMatcher(world: World, pos: BlockPos, blockState: BlockState): AbstractMultiblockMatcher {
        val selected = structureDecider(blockState, world, pos)
        return cachedMatchers.computeIfAbsent(selected.identifier) { selected.toMatcher() }
    }

    fun isBuilt(world: World, pos: BlockPos, blockState: BlockState) = getSelectedMatcher(world, pos, blockState).structureIds.any(isBuilt)

    fun toggleRender(isSneaking: Boolean) {
        if (!isSneaking)
            shouldRenderHologram = !shouldRenderHologram
        else
            variant++
    }
    
    @Environment(EnvType.CLIENT)
    fun render(entity: BlockEntity, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, overlay: Int) {
        if (!shouldRenderHologram) return
        val rotation = AbstractMultiblockMatcher.rotateBlock(entity.cachedState[HorizontalFacingMachineBlock.HORIZONTAL_FACING].opposite)
        getSelectedMatcher(entity.world!!, entity.pos, entity.cachedState).definitions.forEach { def ->
            def.holder.variants.values.toList()[variant % def.holder.variants.size].forEach { (offset, state) ->
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

    fun fromTag(tag: CompoundTag?) {
        shouldRenderHologram = tag?.getBoolean("ShouldRenderHologram") ?: false
        variant = tag?.getInt("Variant") ?: 0
    }

    fun toTag(tag: CompoundTag): CompoundTag {
        tag.putBoolean("ShouldRenderHologram", shouldRenderHologram)
        tag.putInt("Variant", variant)
        return tag
    }
}
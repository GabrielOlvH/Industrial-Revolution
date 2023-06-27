package me.steven.indrev.transportation.client.models

import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
import me.steven.indrev.transportation.utils.blockSpriteId
import me.steven.indrev.transportation.utils.identifier
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

class StoragePipeModel(
    modelIdCollection: MutableList<Identifier>,
    spriteIdCollection: MutableList<SpriteIdentifier>
) : PipeModel(modelIdCollection, spriteIdCollection) {
    val outputMeshes = arrayOfNulls<Mesh>(7)

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        val outputModel = loader.getOrLoadModel(identifier("block/pipe_output"))
        buildRotatedMeshes(outputMeshes, outputModel, loader, textureGetter, modelId)
        return super.bake(loader, textureGetter, rotationContainer, modelId)
    }

    override fun getModelDependencies(): MutableCollection<Identifier> {
        return mutableListOf(*super.getModelDependencies().toTypedArray(), identifier("block/pipe_output"))
    }

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        return mutableListOf(*super.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences).toTypedArray(), blockSpriteId("block/servos"))
    }

    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        super.emitBlockQuads(world, state, pos, randSupplier, context)

        val blockView = world as RenderAttachedBlockView
        val attachment = blockView.getBlockEntityRenderAttachment(pos)
        val config = attachment as? Int2IntOpenHashMap ?: return
        config.forEach { (dirId, modeId) ->
            val meshId = when (dirId) {
                Direction.NORTH.id -> 1
                Direction.EAST.id -> 2
                Direction.SOUTH.id -> 3
                Direction.WEST.id -> 4
                Direction.UP.id -> 5
                Direction.DOWN.id -> 6
                else -> 0
            }
            context.meshConsumer().accept(outputMeshes[meshId])
        }
    }
}
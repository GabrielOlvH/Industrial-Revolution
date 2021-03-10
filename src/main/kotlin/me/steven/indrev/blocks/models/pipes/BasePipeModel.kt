package me.steven.indrev.blocks.models.pipes

import com.mojang.datafixers.util.Pair
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

abstract class BasePipeModel(val tier: Tier, val type: String) : BakedModel, FabricBakedModel, UnbakedModel {

    abstract val spriteIdCollection: MutableList<SpriteIdentifier>
    private val modelIdCollection = mutableListOf(
        identifier("block/${type}_center_${tier.toString().toLowerCase()}"),
        identifier("block/${type}_side_${tier.toString().toLowerCase()}")
    )
    private val spriteArray = arrayOfNulls<Sprite>(4)
    private val modelArray = arrayOfNulls<BakedModel>(7)
    private lateinit var transformation: ModelTransformation

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        modelArray[0] = loader.getOrLoadModel(modelIdCollection[0]).bake(loader, textureGetter, rotationContainer, modelId)
        transformation = modelArray[0]!!.transformation
        val sideModel = loader.getOrLoadModel(modelIdCollection[1])
        modelArray[1] = sideModel.bake(loader, textureGetter, ModelRotation.X270_Y0, modelId) // NORTH
        modelArray[2] = sideModel.bake(loader, textureGetter, ModelRotation.X270_Y90, modelId) // EAST
        modelArray[3] = sideModel.bake(loader, textureGetter, ModelRotation.X270_Y180, modelId) // SOUTH
        modelArray[4] = sideModel.bake(loader, textureGetter, ModelRotation.X270_Y270, modelId) // WEST
        modelArray[5] = sideModel.bake(loader, textureGetter, ModelRotation.X180_Y0, modelId) // UP
        modelArray[6] = sideModel.bake(loader, textureGetter, ModelRotation.X0_Y0, modelId) // DOWN

        spriteIdCollection.forEachIndexed { idx, spriteIdentifier ->
            spriteArray[idx] = textureGetter.apply(spriteIdentifier)
        }
        return this
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = modelIdCollection

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> = spriteIdCollection

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = true

    override fun isBuiltin(): Boolean = false

    override fun getSprite(): Sprite = spriteArray[0]!!

    override fun getTransformation(): ModelTransformation = transformation

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        if (state[BasePipeBlock.COVERED]) {
            val blockEntity = world.getBlockEntity(pos) as? CableBlockEntity
            if (blockEntity?.coverState != null) {
                val coverState = blockEntity.coverState!!
                val model = MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(coverState)
                model.emitFromVanilla(coverState, context, randSupplier) { quad -> !quad.hasColor() }

                context.pushTransform { q ->
                    val rawColor = ColorProviderRegistry.BLOCK[coverState.block]!!.getColor(coverState, world, pos, 0)
                    val color = 255 shl 24 or rawColor
                    q.spriteColor(0, color, color, color, color)
                    true
                }

                model.emitFromVanilla(coverState, context, randSupplier) { quad -> quad.hasColor() }
                context.popTransform()
                if (coverState.isOpaque) return
            }
        }
        handleBakedModel(world, state, pos, randSupplier, context, modelArray[0])
        if (state[BasePipeBlock.NORTH]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[1])
        if (state[BasePipeBlock.EAST]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[2])
        if (state[BasePipeBlock.SOUTH]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[3])
        if (state[BasePipeBlock.WEST]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[4])
        if (state[BasePipeBlock.UP]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[5])
        if (state[BasePipeBlock.DOWN]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[6])
    }

    private fun BakedModel.emitFromVanilla(blockState: BlockState, context: RenderContext, randSupplier: Supplier<Random>, shouldEmit: (BakedQuad) -> Boolean) {
        val emitter = context.emitter
        Direction.values().forEach { dir ->
            getQuads(blockState, dir, randSupplier.get()).forEach { quad ->
                if (shouldEmit(quad)) {
                    emitter.fromVanilla(quad.vertexData, 0, false)
                    emitter.emit()
                }
            }
        }
        getQuads(blockState, null, randSupplier.get()).forEach { quad ->
            if (shouldEmit(quad)) {
                emitter.fromVanilla(quad.vertexData, 0, false)
                emitter.emit()
            }
        }
    }

    private fun handleBakedModel(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext,
        bakedModel: BakedModel?) {
        if (bakedModel is FabricBakedModel) bakedModel.emitBlockQuads(world, state, pos, randSupplier, context)
        else if (bakedModel != null) context.fallbackConsumer().accept(bakedModel)
    }

    override fun emitItemQuads(stack: ItemStack?, p1: Supplier<Random>, context: RenderContext) {
        context.fallbackConsumer().accept(modelArray[0])
    }
}
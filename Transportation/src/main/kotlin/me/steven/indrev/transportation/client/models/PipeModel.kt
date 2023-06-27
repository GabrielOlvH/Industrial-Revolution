package me.steven.indrev.transportation.client.models

import com.mojang.datafixers.util.Pair
import me.steven.indrev.transportation.blocks.PipeBlock
import me.steven.indrev.transportation.networks.ClientPipeNetworkData
import me.steven.indrev.transportation.utils.PipeConnections
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView

import java.util.function.Function
import java.util.function.Supplier

open class PipeModel(
    val modelIdCollection: MutableList<Identifier>,
    val spriteIdCollection: MutableList<SpriteIdentifier>
    ) : BakedModel, FabricBakedModel, UnbakedModel {

    val spriteArray = arrayOfNulls<Sprite>(1)
    private val meshArray = arrayOfNulls<Mesh>(7)
    lateinit var transform: ModelTransformation

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        spriteIdCollection.forEachIndexed { idx, spriteIdentifier ->
            spriteArray[idx] = textureGetter.apply(spriteIdentifier)
        }
        val center = loader.getOrLoadModel(modelIdCollection[0]).bake(loader, textureGetter, rotationContainer, modelId)!!
        meshArray[0] = buildDefaultMesh(center)
        transform = center.transformation
        val sideModel = loader.getOrLoadModel(modelIdCollection[1])
        buildRotatedMeshes(meshArray, sideModel, loader, textureGetter, modelId)
        return this
    }

    fun buildRotatedMeshes(array: Array<Mesh?>, model: UnbakedModel, loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, modelId: Identifier?) {
        array[1] = buildDefaultMesh(model.bake(loader, textureGetter, ModelRotation.X270_Y0, modelId)!!) // NORTH
        array[2] = buildDefaultMesh(model.bake(loader, textureGetter, ModelRotation.X270_Y90, modelId)!!) // EAST
        array[3] = buildDefaultMesh(model.bake(loader, textureGetter, ModelRotation.X270_Y180, modelId)!!) // SOUTH
        array[4] = buildDefaultMesh(model.bake(loader, textureGetter, ModelRotation.X270_Y270, modelId)!!) // WEST
        array[5] = buildDefaultMesh(model.bake(loader, textureGetter, ModelRotation.X180_Y0, modelId)!!) // UP
        array[6] = buildDefaultMesh(model.bake(loader, textureGetter, ModelRotation.X0_Y0, modelId)!!) // DOWN
    }

    fun buildDefaultMesh(model: BakedModel): Mesh {
        val renderer: Renderer = RendererAccess.INSTANCE.renderer!!
        val builder: MeshBuilder = renderer.meshBuilder()
        val emitter = builder.emitter
        model.getQuads(null, null, null).forEach { q ->
            emitter.fromVanilla(q, null, null)
            emitter.emit()
        }
        return builder.build()
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

    override fun getParticleSprite(): Sprite = spriteArray[0]!!

    override fun getTransformation(): ModelTransformation = transform

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        /*   val renderData = (world as RenderAttachedBlockView).getBlockEntityRenderAttachment(pos) as? BasePipeBlockEntity.PipeRenderData ?: return
        if (renderData.cover != null) {
            val coverState = renderData.cover
            val model = MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(coverState)
            val color =
                255 shl 24 or (ColorProviderRegistry.BLOCK[coverState.block]?.getColor(coverState, world, pos, 0) ?: -1)

            val emitter = context.emitter
            val renderLayer = BlendMode.fromRenderLayer(RenderLayers.getBlockLayer(coverState))
            val material = RENDER_LAYER_MATERIAL_MAP.computeIfAbsent(renderLayer) {
                RendererAccess.INSTANCE.renderer!!.materialFinder().blendMode(0, renderLayer).find()
            }
            DIRECTIONS.forEach { dir ->
                model.getQuads(coverState, dir, randSupplier.get()).forEach { quad ->
                    emitter.fromVanilla(quad, material, dir)
                    if (quad.hasColor()) {
                        emitter.spriteColor(0, color, color, color, color)
                    }
                    emitter.emit()
                }
            }
            if (coverState.isOpaque) return
        }*/

        context.meshConsumer().accept(meshArray[0])
        val value = ClientPipeNetworkData.renderData.get(pos.asLong())
        if (value != -1) {
            val connections = PipeConnections(value)

            if (connections.contains(Direction.NORTH)) context.meshConsumer().accept(meshArray[1])
            if (connections.contains(Direction.EAST)) context.meshConsumer().accept(meshArray[2])
            if (connections.contains(Direction.SOUTH)) context.meshConsumer().accept(meshArray[3])
            if (connections.contains(Direction.WEST)) context.meshConsumer().accept(meshArray[4])
            if (connections.contains(Direction.UP)) context.meshConsumer().accept(meshArray[5])
            if (connections.contains(Direction.DOWN)) context.meshConsumer().accept(meshArray[6])
        }
    }

    override fun emitItemQuads(stack: ItemStack?, p1: Supplier<Random>, context: RenderContext) {
        context.meshConsumer().accept(meshArray[0])
    }

    companion object {
        val RENDER_LAYER_MATERIAL_MAP = hashMapOf<BlendMode, RenderMaterial>()
        val DIRECTIONS = Direction.values()
    }
}
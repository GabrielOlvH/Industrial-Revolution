package me.steven.indrev.blocks.models.pipes

import com.mojang.datafixers.util.Pair
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.cables.CoverableBlockEntity
import me.steven.indrev.blocks.machine.pipes.BasePipeBlock
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.fabric.api.renderer.v1.Renderer
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
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
    protected val spriteArray = arrayOfNulls<Sprite>(4)
    protected val meshArray = arrayOfNulls<Mesh>(7)
    private lateinit var transformation: ModelTransformation

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
        meshArray[0] = buildDefaultMesh(0, center)
        transformation = center.transformation
        val sideModel = loader.getOrLoadModel(modelIdCollection[1])
        meshArray[1] = buildDefaultMesh(1, sideModel.bake(loader, textureGetter, ModelRotation.X270_Y0, modelId)!!) // NORTH
        meshArray[2] = buildDefaultMesh(2, sideModel.bake(loader, textureGetter, ModelRotation.X270_Y90, modelId)!!) // EAST
        meshArray[3] = buildDefaultMesh(3, sideModel.bake(loader, textureGetter, ModelRotation.X270_Y180, modelId)!!)// SOUTH
        meshArray[4] = buildDefaultMesh(4, sideModel.bake(loader, textureGetter, ModelRotation.X270_Y270, modelId)!!)// WEST
        meshArray[5] = buildDefaultMesh(5, sideModel.bake(loader, textureGetter, ModelRotation.X180_Y0, modelId)!!) // UP
        meshArray[6] = buildDefaultMesh(6, sideModel.bake(loader, textureGetter, ModelRotation.X0_Y0, modelId)!!) // DOWN

        return this
    }

    protected open fun buildDefaultMesh(index: Int, model: BakedModel): Mesh {
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
            val blockEntity = world.getBlockEntity(pos) as? CoverableBlockEntity
            if (blockEntity?.coverState != null) {
                val coverState = blockEntity.coverState!!
                val model = MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(coverState)
                val color = 255 shl 24 or (ColorProviderRegistry.BLOCK[coverState.block]?.getColor(coverState, world, pos, 0) ?: -1)

                val emitter = context.emitter
                DIRECTIONS.forEach { dir ->
                    model.getQuads(coverState, dir, randSupplier.get()).forEach { quad ->
                        emitter.fromVanilla(quad, TRANSLUCENT, dir)
                        if (quad.hasColor()) {
                            emitter.spriteColor(0, color, color, color, color)
                        }
                        emitter.emit()
                    }
                }
                if (coverState.isOpaque) return
            }
        }

        context.meshConsumer().accept(meshArray[0])
        if (state[BasePipeBlock.NORTH]) context.meshConsumer().accept(meshArray[1])
        if (state[BasePipeBlock.EAST]) context.meshConsumer().accept(meshArray[2])
        if (state[BasePipeBlock.SOUTH]) context.meshConsumer().accept(meshArray[3])
        if (state[BasePipeBlock.WEST]) context.meshConsumer().accept(meshArray[4])
        if (state[BasePipeBlock.UP]) context.meshConsumer().accept(meshArray[5])
        if (state[BasePipeBlock.DOWN]) context.meshConsumer().accept(meshArray[6])
    }

    override fun emitItemQuads(stack: ItemStack?, p1: Supplier<Random>, context: RenderContext) {
        context.meshConsumer().accept(meshArray[0])
    }

    companion object {
        val TRANSLUCENT: RenderMaterial by lazy {
            RendererAccess.INSTANCE.renderer!!.materialFinder().blendMode(0, BlendMode.TRANSLUCENT).find()
        }
        val DIRECTIONS = Direction.values()
    }
}
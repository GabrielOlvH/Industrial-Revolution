package me.steven.indrev.blocks

import com.mojang.datafixers.util.Pair
import me.steven.indrev.blockentities.cables.CableBlockEntity
import me.steven.indrev.blocks.machine.CableBlock
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
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
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class CableModel(val tier: Tier) : BakedModel, FabricBakedModel, UnbakedModel {

    private val spriteIdCollection = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/cable_center")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/cable_center_emissive_${tier.toString().toLowerCase()}")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/cable_wrap")),
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/cable_wire_emissive_${tier.toString().toLowerCase()}"))
    )
    private val modelIdCollection = mutableListOf(
        identifier("block/cable_center_${tier.toString().toLowerCase()}"),
        identifier("block/cable_side_${tier.toString().toLowerCase()}")
    )
    private val spriteArray = arrayOfNulls<Sprite>(4)
    private val modelArray = arrayOfNulls<BakedModel>(7)

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel? {
        modelArray[0] = loader.getOrLoadModel(modelIdCollection[0]).bake(loader, textureGetter, rotationContainer, modelId)
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

    override fun getTransformation(): ModelTransformation {
        return MinecraftClient.getInstance().bakedModelManager.getModel(ModelIdentifier(Identifier("stone"), "")).transformation
    }

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        world: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randSupplier: Supplier<Random>,
        context: RenderContext
    ) {
        if (state[CableBlock.COVERED]) {
            val blockEntity = world.getBlockEntity(pos) as? CableBlockEntity
            if (blockEntity?.cover != null) {
                var emitter = context.emitter
                val coverState = Registry.BLOCK.get(blockEntity.cover).defaultState
                val model = MinecraftClient.getInstance().bakedModelManager.blockModels.getModel(coverState)
                Direction.values().forEach { dir ->
                    model.getQuads(null, dir, randSupplier.get()).forEach { quad ->
                        if (!quad.hasColor()) {
                            emitter.fromVanilla(quad.vertexData, 0, false)
                            emitter.emit()
                        }
                    }
                }
                model.getQuads(null, null, randSupplier.get()).forEach { quad ->
                    if (!quad.hasColor()) {
                        emitter.fromVanilla(quad.vertexData, 0, false)
                        emitter.emit()
                    }
                }
                context.pushTransform { q ->
                    val rawColor = ColorProviderRegistry.BLOCK[coverState.block]!!.getColor(coverState, world, pos, 0)
                    val color = 255 shl 24 or rawColor
                    q.spriteColor(0, color, color, color, color)
                    true
                }
                emitter = context.emitter
                Direction.values().forEach { dir ->
                    model.getQuads(null, dir, randSupplier.get()).forEach { quad ->
                        if (quad.hasColor()) {
                            emitter.fromVanilla(quad.vertexData, 0, false)
                            emitter.emit()
                        }
                    }
                }
                model.getQuads(null, null, randSupplier.get()).forEach { quad ->
                    if (quad.hasColor()) {
                        emitter.fromVanilla(quad.vertexData, 0, false)
                        emitter.emit()
                    }
                }
                context.popTransform()
                if (coverState.isOpaque) return
            }
        }
        handleBakedModel(world, state, pos, randSupplier, context, modelArray[0])
        if (state[CableBlock.NORTH]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[1])
        if (state[CableBlock.EAST]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[2])
        if (state[CableBlock.SOUTH]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[3])
        if (state[CableBlock.WEST]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[4])
        if (state[CableBlock.UP]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[5])
        if (state[CableBlock.DOWN]) handleBakedModel(world, state, pos, randSupplier, context, modelArray[6])
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
        val state = MachineRegistry.CABLE_REGISTRY.block(tier).defaultState
        if (state[CableBlock.NORTH]) context.fallbackConsumer().accept(modelArray[1])
        if (state[CableBlock.EAST]) context.fallbackConsumer().accept(modelArray[2])
        if (state[CableBlock.SOUTH]) context.fallbackConsumer().accept(modelArray[3])
        if (state[CableBlock.WEST]) context.fallbackConsumer().accept(modelArray[4])
        if (state[CableBlock.UP]) context.fallbackConsumer().accept(modelArray[5])
        if (state[CableBlock.DOWN]) context.fallbackConsumer().accept(modelArray[6])
    }
}
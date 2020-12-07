package me.steven.indrev.blocks.containers

import com.mojang.datafixers.util.Pair
import me.steven.indrev.blockentities.storage.BatteryBlockEntity
import me.steven.indrev.utils.TransferMode
import me.steven.indrev.utils.identifier
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
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class LazuliFluxContainerBakedModel(val id: String) : UnbakedModel, BakedModel, FabricBakedModel {

    private val spriteIdCollection = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/lazuli_flux_container"))
    )

    private val modelIdentifier = identifier("block/lazuli_flux_container")

    private var bakedModel: BakedModel? = null
    private val inputModels: Array<BakedModel?> = arrayOfNulls(Direction.values().size)
    private val outputModels: Array<BakedModel?> = arrayOfNulls(Direction.values().size)
    private val energyLevelModels: Array<BakedModel?> = arrayOfNulls(Direction.values().size)

    private val color: Int = when (id.last()) {
        '1' -> 0xffbb19
        '2' -> 0x5d3dff
        '3' -> 0xfd47ff
        else -> 0xff4070
    }

    private var sprite: Sprite? = null

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        bakedModel = loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, rotationContainer, modelId)
        sprite = textureGetter.apply(spriteIdCollection[0])
        val inputModel = loader.getOrLoadModel(identifier("block/lazuli_flux_container_input"))
        val outputModel = loader.getOrLoadModel(identifier("block/lazuli_flux_container_output"))
        val energyLevelModel = loader.getOrLoadModel(identifier("block/lazuli_flux_container_item_lf_level"))
        Direction.values().forEach { dir ->
            val rotation = getModelRotationFacingDown(dir)
            inputModels[dir.id] = inputModel.bake(loader, textureGetter, rotation, modelId)
            outputModels[dir.id] = outputModel.bake(loader, textureGetter, rotation, modelId)
            if (dir.axis.isHorizontal)
                energyLevelModels[dir.id] = energyLevelModel.bake(loader, textureGetter, rotation, modelId)
        }
        return this
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        return spriteIdCollection
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
        mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = true

    override fun isBuiltin(): Boolean = false

    override fun getSprite(): Sprite? = sprite

    override fun getTransformation(): ModelTransformation = MinecraftClient.getInstance().bakedModelManager.getModel(
        ModelIdentifier(Identifier("stone"), "")
    ).transformation

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false


    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?,
        ctx: RenderContext
    ) {
        ctx.fallbackConsumer().accept(bakedModel)
        val blockEntity = blockView?.getBlockEntity(pos) as? BatteryBlockEntity ?: return
        blockEntity.transferConfig.forEach { dir, mode ->
            val modelArray =
                when (mode) {
                    TransferMode.OUTPUT -> outputModels
                    TransferMode.INPUT -> inputModels
                    else -> return@forEach
                }
            ctx.fallbackConsumer().accept(modelArray[dir.id])
        }
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        ctx.fallbackConsumer().accept(bakedModel)
        ctx.pushTransform { quad ->
            val color = 255 shl 24 or color
            quad.spriteColor(0, color, color, color, color)
            true
        }
        energyLevelModels.forEach {
            ctx.fallbackConsumer().accept(it ?: return@forEach)
        }
        ctx.popTransform()
    }


    private fun getModelRotationFacingDown(direction: Direction): ModelRotation {
        return when (direction) {
            Direction.DOWN -> ModelRotation.X90_Y0
            Direction.UP -> ModelRotation.X270_Y180
            Direction.NORTH -> ModelRotation.X0_Y0
            Direction.SOUTH -> ModelRotation.X0_Y180
            Direction.WEST -> ModelRotation.X0_Y270
            Direction.EAST -> ModelRotation.X0_Y90
        }
    }
}
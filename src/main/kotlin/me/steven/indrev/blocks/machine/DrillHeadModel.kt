package me.steven.indrev.blocks.machine

import com.mojang.datafixers.util.Pair
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

class DrillHeadModel(val variant: String) : UnbakedModel, BakedModel, FabricBakedModel {

    private val spriteIdCollection = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("item/${variant}_drill_head"))
    )

    private val modelIdentifier = identifier("block/${variant}_drill_head")
    private var bakedModel: BakedModel? = null

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel? {
        bakedModel = loader.getOrLoadModel(modelIdentifier).bake(loader, textureGetter, rotationContainer, modelId)
        return bakedModel
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        return spriteIdCollection
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    override fun getSprite(): Sprite? =  null

    override fun getTransformation(): ModelTransformation = MinecraftClient.getInstance().bakedModelManager.getModel(
        ModelIdentifier(Identifier("stone"), "")
    ).transformation

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        p0: BlockRenderView?,
        p1: BlockState?,
        p2: BlockPos?,
        p3: Supplier<Random>?,
        ctx: RenderContext
    ) {
        ctx.fallbackConsumer().accept(bakedModel)
    }

    override fun emitItemQuads(p0: ItemStack?, p1: Supplier<Random>?, p2: RenderContext?) {
    }

}
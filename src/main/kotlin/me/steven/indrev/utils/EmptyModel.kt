package me.steven.indrev.utils

import com.mojang.datafixers.util.Pair
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

object EmptyModel : UnbakedModel, BakedModel, FabricBakedModel {
    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> = mutableListOf()

    override fun bake(
        loader: ModelLoader?,
        textureGetter: Function<SpriteIdentifier, Sprite>?,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel = this

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun useAmbientOcclusion(): Boolean = false

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    override fun getParticleSprite(): Sprite? = null

    override fun getTransformation(): ModelTransformation = ModelTransformation.NONE

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState?,
        pos: BlockPos?,
        randomSupplier: Supplier<Random>?,
        context: RenderContext?
    ) {
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
    }
}
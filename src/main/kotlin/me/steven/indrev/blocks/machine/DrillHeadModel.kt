package me.steven.indrev.blocks.machine

import com.mojang.datafixers.util.Pair
import me.steven.indrev.utils.identifier
import net.minecraft.client.render.model.*
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.Identifier
import java.util.function.Function

class DrillHeadModel(val variant: String) : UnbakedModel {

    private val spriteIdCollection = mutableListOf(
        SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("item/${variant}_drill_head"))
    )

    private val modelIdentifier = identifier("block/${variant}_drill_head")
    private var bakedModel: BakedModel? = null

    override fun bake(
        baker: Baker,
        textureGetter: Function<SpriteIdentifier, Sprite>?,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel? {
        bakedModel = baker.getOrLoadModel(modelIdentifier).bake(baker, textureGetter, rotationContainer, modelId)
        return bakedModel
    }

    override fun setParents(modelLoader: Function<Identifier, UnbakedModel>?) {

    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()
    /*override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        return spriteIdCollection
    }*/
}
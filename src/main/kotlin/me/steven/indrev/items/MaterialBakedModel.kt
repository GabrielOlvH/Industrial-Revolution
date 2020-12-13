package me.steven.indrev.items

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
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

class MaterialBakedModel private constructor(private val bakedModels: Array<ModelWithColor>, private val type: TransformationType) : UnbakedModel, BakedModel, FabricBakedModel {

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        return this
    }

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        return mutableListOf()
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
        mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = type == TransformationType.BLOCK

    override fun isBuiltin(): Boolean = false

    override fun getSprite(): Sprite? = null

    override fun getTransformation(): ModelTransformation = type.transformation()

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        blockView: BlockRenderView?,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        if (type == TransformationType.BLOCK) {
            bakedModels.forEach { holder ->
                val color = holder.color
                ctx.pushTransform { quad ->
                    quad.spriteColor(0, color, color, color, color)
                    true
                }
                ctx.fallbackConsumer().accept(holder.blockBakedModel)
                ctx.popTransform()
            }
        }
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        bakedModels.forEach { holder ->
            val color = holder.color
            ctx.pushTransform { quad ->
                quad.spriteColor(0, color, color, color, color)
                true
            }
            ctx.fallbackConsumer().accept(holder.itemBakedModel)
            ctx.popTransform()
        }

    }


    class Builder {

        private val bakedModels: MutableList<ModelWithColor> = mutableListOf()
        private var transformationType: TransformationType = TransformationType.AUTO

        fun with(id: Identifier, color: Long): Builder {
            bakedModels.add(ModelWithColor(id, color.toInt()))
            return this
        }

        fun ingotBase(color: Long): Builder {
            return with(identifier("ingot_base"), color)
        }

        fun ingotShadow(color: Long): Builder {
            return with(identifier("ingot_shadow"), color)
        }

        fun ingotHighlight(color: Long): Builder {
            return with(identifier("ingot_highlight"), color)
        }

        fun stick(): Builder {
            transformationType = TransformationType.HANDHELD
            return with(Identifier("stick"), -1)
        }

        fun block(): Builder {
            transformationType = TransformationType.BLOCK
            return this
        }

        fun build() = MaterialBakedModel(bakedModels.toTypedArray(), transformationType)
    }

    enum class TransformationType(val transformation: () -> ModelTransformation) {
        AUTO(
            {
                MinecraftClient.getInstance().bakedModelManager.getModel(
                    ModelIdentifier(Identifier("iron_ingot"), "inventory")
                ).transformation
            }
        ),
        HANDHELD(
            {
                MinecraftClient.getInstance().bakedModelManager.getModel(
                    ModelIdentifier(Identifier("stick"), "inventory")
                ).transformation
            }
        ),
        BLOCK(
            {
                MinecraftClient.getInstance().bakedModelManager.getModel(
                    ModelIdentifier(Identifier("stone"), "")
                ).transformation
            }
        )
    }
}
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

        fun plateBase(color: Long): Builder {
            return with(identifier("plate_base"), color)
        }

        fun plateHighlight(color: Long): Builder {
            return with(identifier("plate_highlight"), color)
        }

        fun pickaxeBase(color: Long): Builder {
            return with(identifier("pickaxe_base"), color)
        }

        fun pickaxeShadow(color: Long): Builder {
            return with(identifier("pickaxe_shadow"), color)
        }

        fun pickaxeHighlight(color: Long): Builder {
            return with(identifier("pickaxe_highlight"), color)
        }

        fun axeBase(color: Long): Builder {
            return with(identifier("axe_base"), color)
        }

        fun axeShadow(color: Long): Builder {
            return with(identifier("axe_shadow"), color)
        }

        fun axeHighlight(color: Long): Builder {
            return with(identifier("axe_highlight"), color)
        }

        fun helmetBase(color: Long): Builder {
            return with(identifier("helmet_base"), color)
        }

        fun helmetShadow(color: Long): Builder {
            return with(identifier("helmet_shadow"), color)
        }

        fun helmetHighlight(color: Long): Builder {
            return with(identifier("helmet_highlight"), color)
        }

        fun dustBase(color: Long): Builder {
            return with(identifier("dust_base"), color)
        }

        fun dustShadow(color: Long): Builder {
            return with(identifier("dust_shadow"), color)
        }

        fun dustHighlight(color: Long): Builder {
            return with(identifier("dust_highlight"), color)
        }

        fun chestplateBase(color: Long): Builder {
            return with(identifier("chestplate_base"), color)
        }

        fun chestplateShadow(color: Long): Builder {
            return with(identifier("chestplate_shadow"), color)
        }

        fun chestplateHighlight(color: Long): Builder {
            return with(identifier("chestplate_highlight"), color)
        }

        fun bootsBase(color: Long): Builder {
            return with(identifier("boots_base"), color)
        }

        fun bootsShadow(color: Long): Builder {
            return with(identifier("boots_shadow"), color)
        }

        fun bootsHighlight(color: Long): Builder {
            return with(identifier("boots_highlight"), color)
        }

        fun hoeBase(color: Long): Builder {
            return with(identifier("hoe_base"), color)
        }

        fun hoeShadow(color: Long): Builder {
            return with(identifier("hoe_shadow"), color)
        }

        fun hoeHighlight(color: Long): Builder {
            return with(identifier("hoe_highlight"), color)
        }

        fun leggingsBase(color: Long): Builder {
            return with(identifier("leggings_base"), color)
        }

        fun leggingsShadow(color: Long): Builder {
            return with(identifier("leggings_shadow"), color)
        }

        fun leggingsHighlight(color: Long): Builder {
            return with(identifier("leggings_highlight"), color)
        }

        fun nuggetBase(color: Long): Builder {
            return with(identifier("nugget_base"), color)
        }

        fun nuggetShadow(color: Long): Builder {
            return with(identifier("nugget_shadow"), color)
        }

        fun nuggetHighlight(color: Long): Builder {
            return with(identifier("nugget_highlight"), color)
        }

        fun oreBase(color: Long): Builder {
            return with(identifier("ore_base"), color)
        }

        fun oreHighlight(color: Long): Builder {
            return with(identifier("ore_highlight"), color)
        }

        fun blockBase(color: Long): Builder {
            return with(identifier("block_base"), color)
        }

        fun blockShadow(color: Long): Builder {
            return with(identifier("block_shadow"), color)
        }

        fun blockHighlight(color: Long): Builder {
            return with(identifier("block_highlight"), color)
        }

        fun purifiedOreBase(color: Long): Builder {
            return with(identifier("purified_ore_base"), color)
        }

        fun purifiedOreShadow(color: Long): Builder {
            return with(identifier("purified_ore_shadow"), color)
        }

        fun purifiedOreHighlight(color: Long): Builder {
            return with(identifier("purified_ore_highlight"), color)
        }

        fun chunkBase(color: Long): Builder {
            return with(identifier("chunk_base"), color)
        }

        fun chunkShadow(color: Long): Builder {
            return with(identifier("chunk_shadow"), color)
        }

        fun chunkHighlight(color: Long): Builder {
            return with(identifier("chunk_highlight"), color)
        }

        fun shovelBase(color: Long): Builder {
            return with(identifier("shovel_base"), color)
        }

        fun shovelShadow(color: Long): Builder {
            return with(identifier("shovel_shadow"), color)
        }

        fun shovelHighlight(color: Long): Builder {
            return with(identifier("shovel_highlight"), color)
        }

        fun swordBase(color: Long): Builder {
            return with(identifier("sword_base"), color)
        }

        fun swordShadow(color: Long): Builder {
            return with(identifier("sword_shadow"), color)
        }

        fun swordHighlight(color: Long): Builder {
            return with(identifier("sword_highlight"), color)
        }

        fun toolStick(): Builder {
            transformationType = TransformationType.HANDHELD
            return with(identifier("tool_stick"), -1)
        }

        fun block(): Builder {
            transformationType = TransformationType.BLOCK
            return with(Identifier("stone"), -1)
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
                    ModelIdentifier(identifier("tool_stick"), "inventory")
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

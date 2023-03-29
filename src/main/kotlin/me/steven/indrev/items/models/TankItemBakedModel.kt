package me.steven.indrev.items.models

import com.mojang.datafixers.util.Pair
import me.steven.indrev.utils.IRFluidAmount
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockRenderView
import java.util.function.Function
import java.util.function.Supplier

class TankItemBakedModel : UnbakedModel, BakedModel, FabricBakedModel {

    private val modelIdentifier = ModelIdentifier(
        identifier("tank"),
        "down=false,up=false"
    )

    private val transform: ModelTransformation by lazy {
        MinecraftClient.getInstance().bakedModelManager.getModel(
            ModelIdentifier(Identifier("stone"), "")
        ).transformation
    }

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {
        val tankModel = MinecraftClient.getInstance().bakedModelManager.getModel(
            modelIdentifier
        )
        context.fallbackConsumer().accept(tankModel)

        val stackTag = stack.orCreateNbt

        val volume = readNbt(stackTag) ?: return

        val player = MinecraftClient.getInstance().player
        val world = player?.world
        val pos = player?.blockPos

        val fluid = volume.resource.fluid ?: Fluids.EMPTY
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val fluidColor = fluidRenderHandler.getFluidColor(world, pos, fluid.defaultState)
        val fluidSprite = fluidRenderHandler.getFluidSprites(world, pos, fluid.defaultState)[0]
        val color = 255 shl 24 or fluidColor
        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val emitter = context.emitter

        val p = (volume.amount()/81000f / 8f).coerceAtMost(0.9f)
        emitter.draw(Direction.UP, fluidSprite, 0.09375f, 0.09f, 0.9f, 0.90625f, (0.9f - p) + 0.09575f)
        emitter.draw(Direction.NORTH, fluidSprite, 0.09375f, 0.06f, 0.9f, p, 0.09575f)
        emitter.draw(Direction.SOUTH, fluidSprite, 0.09375f, 0.06f, 0.9f, p, 0.09575f)
        emitter.draw(Direction.EAST, fluidSprite, 0.09375f, 0.06f, 0.9f, p, 0.09575f)
        emitter.draw(Direction.WEST, fluidSprite, 0.09375f, 0.06f, 0.9f, p, 0.09575f)

        context.popTransform()
    }

    private fun QuadEmitter.draw(
        side: Direction,
        sprite: Sprite,
        left: Float,
        bottom: Float,
        right: Float,
        top: Float,
        depth: Float
    ) {
        square(side, left, bottom, right, top, depth)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0, -1, -1, -1, -1)
        emit()
    }

    override fun emitBlockQuads(
        p0: BlockRenderView?,
        p1: BlockState?,
        p2: BlockPos?,
        p3: Supplier<Random>?,
        p4: RenderContext?
    ) {
    }

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getParticleSprite() = null

    override fun hasDepth(): Boolean = false

    override fun getTransformation(): ModelTransformation = transform

    override fun useAmbientOcclusion(): Boolean = true

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    private fun readNbt(tag: NbtCompound?): IRFluidAmount? {
        val tanksTag = tag?.getCompound("tanks")

        tanksTag?.keys?.forEach { key ->
            val index = key.toInt()
            val tankTag = tanksTag.getCompound(key).getCompound("fluids")
            return IRFluidAmount(FluidVariant.fromNbt(tankTag.getCompound("variant")), tankTag.getLong("amt"))
        }
        return null
    }

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
}
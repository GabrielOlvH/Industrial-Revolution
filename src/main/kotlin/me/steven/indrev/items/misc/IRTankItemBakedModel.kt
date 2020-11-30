package me.steven.indrev.items.misc

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.BakedModel
import net.minecraft.client.render.model.BakedQuad
import net.minecraft.client.render.model.json.JsonUnbakedModel
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.util.ModelIdentifier
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.Reader
import java.util.*
import java.util.function.Supplier

object IRTankItemBakedModel : BakedModel, FabricBakedModel {

    private val modelIdentifier = ModelIdentifier(
        identifier("tank"),
        "down=false,up=false"
    )

    override fun isVanillaAdapter(): Boolean = false

    override fun emitItemQuads(stack: ItemStack, randSupplier: Supplier<Random>, context: RenderContext) {
        val tankModel = MinecraftClient.getInstance().bakedModelManager.getModel(
            modelIdentifier
        )
        context.fallbackConsumer().accept(tankModel)

        val stackTag = stack.orCreateTag

        val volume = fromTag(stackTag) ?: return

        val player = MinecraftClient.getInstance().player
        val world = player?.world
        val pos = player?.blockPos

        val fluid = volume.rawFluid ?: Fluids.EMPTY
        val fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluid) ?: return
        val fluidColor = fluidRenderHandler.getFluidColor(world, pos, fluid.defaultState)
        val fluidSprite = fluidRenderHandler.getFluidSprites(world, pos, fluid.defaultState)[0]
        val color = 255 shl 24 or fluidColor
        context.pushTransform { quad ->
            quad.spriteColor(0, color, color, color, color)
            true
        }

        val emitter = context.emitter

        val p = (volume.amount().asLong(1L) / 8f).coerceAtMost(0.9f)
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

    @Throws(IOException::class)
    private fun getReaderForResource(location: Identifier): Reader {
        val file = Identifier(location.namespace, location.path + ".json")
        val resource = MinecraftClient.getInstance().resourceManager.getResource(file)
        return BufferedReader(InputStreamReader(resource.inputStream, Charsets.UTF_8))
    }

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> = mutableListOf()

    override fun getSprite() = null

    override fun hasDepth(): Boolean = false

    private val transform: ModelTransformation? by lazy {
        loadTransformFromJson(Identifier("minecraft:models/block/block"))
    }

    override fun getTransformation(): ModelTransformation? = transform

    override fun useAmbientOcclusion(): Boolean = true

    override fun isSideLit(): Boolean = false

    override fun isBuiltin(): Boolean = false

    private fun loadTransformFromJson(location: Identifier): ModelTransformation? {
        return try {
            JsonUnbakedModel.deserialize(getReaderForResource(location)).transformations
        } catch (exception: IOException) {
            exception.printStackTrace()
            null
        }
    }
    fun fromTag(tag: CompoundTag?): FluidVolume? {
        val tanksTag = tag?.getCompound("tanks")
        tanksTag?.keys?.forEach { key ->
            val tankTag = tanksTag.getCompound(key)
            return FluidVolume.fromTag(tankTag.getCompound("fluids"))
        }
        return null
    }
}
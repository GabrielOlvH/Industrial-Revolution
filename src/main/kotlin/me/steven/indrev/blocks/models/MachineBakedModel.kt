package me.steven.indrev.blocks.models

import com.mojang.datafixers.util.Pair
import me.steven.indrev.api.machines.Tier
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blocks.machine.MachineBlock
import me.steven.indrev.utils.blockSpriteId
import me.steven.indrev.utils.identifier
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.MissingSprite
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

open class MachineBakedModel(id: String) : UnbakedModel, BakedModel, FabricBakedModel {

    var baseSprite = SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, identifier("block/${id.replace(Regex("_mk[0-4]"), "")}"))

    var overlayIds: MutableList<SpriteIdentifier> = mutableListOf()
    var workingOverlayIds: MutableList<SpriteIdentifier> = mutableListOf()

    private var sprite: Sprite? = null
    protected val overlays: Array<Sprite?> by lazy { arrayOfNulls(overlayIds.size) }
    protected val workingOverlays: Array<Sprite?> by lazy { arrayOfNulls(workingOverlayIds.size) }

    protected val emissives = hashSetOf<Sprite>()

    fun factoryOverlay() {
        overlayIds.add(blockSpriteId("block/factory_overlay"))
    }

    fun tierOverlay(tier: Tier) {
        overlayIds.add(blockSpriteId("block/${tier.toString().toLowerCase()}_overlay"))
    }

    override fun bake(
        loader: ModelLoader,
        textureGetter: Function<SpriteIdentifier, Sprite>,
        rotationContainer: ModelBakeSettings?,
        modelId: Identifier?
    ): BakedModel {
        sprite = textureGetter.apply(baseSprite)
        overlayIds.processSprites(overlays, textureGetter)
        workingOverlayIds.processSprites(workingOverlays, textureGetter)
        if (isEmissive(sprite)) emissives.add(sprite!!)
        return this
    }

    private fun List<SpriteIdentifier>.processSprites(arr: Array<Sprite?>, textureGetter: Function<SpriteIdentifier, Sprite>) {
        forEachIndexed { index, id ->
            val sprite = textureGetter.apply(id)
            if (sprite.id != MissingSprite.getMissingSpriteId()) {
                arr[index] = sprite
                if (isEmissive(sprite))
                    emissives.add(sprite)
            }
        }
    }

    //don't judge me
    private fun isEmissive(sprite: Sprite?) = sprite?.id?.toString()?.contains("emissive") == true

    override fun getModelDependencies(): MutableCollection<Identifier> = mutableListOf()

    override fun getTextureDependencies(
        unbakedModelGetter: Function<Identifier, UnbakedModel>?,
        unresolvedTextureReferences: MutableSet<Pair<String, String>>?
    ): MutableCollection<SpriteIdentifier> {
        val list = mutableListOf(baseSprite)
        list.addAll(overlayIds)
        list.addAll(workingOverlayIds)
        return list
    }

    override fun getQuads(state: BlockState?, face: Direction?, random: Random?): MutableList<BakedQuad> =
        mutableListOf()

    override fun useAmbientOcclusion(): Boolean = true

    override fun hasDepth(): Boolean = false

    override fun isSideLit(): Boolean = true

    override fun isBuiltin(): Boolean = false

    override fun getSprite(): Sprite? = sprite

    override fun getTransformation(): ModelTransformation = TRANSFORM

    override fun getOverrides(): ModelOverrideList = ModelOverrideList.EMPTY

    override fun isVanillaAdapter(): Boolean = false

    override fun emitBlockQuads(
        blockView: BlockRenderView,
        state: BlockState,
        pos: BlockPos,
        randomSupplier: Supplier<Random>,
        ctx: RenderContext
    ) {
        val block = state.block as? MachineBlock ?: return
        val direction = block.getFacing(state)

        emitQuads(direction, sprite!!, ctx)
        if (workingOverlays.isNotEmpty()) {
            val blockEntity = blockView.getBlockEntity(pos) as? MachineBlockEntity<*> ?: return
            if (blockEntity.workingState) {
                workingOverlays.forEach { emitQuads(direction, it!!, ctx) }
            }
        }
        if (overlays.isNotEmpty())
            overlays.forEach { emitQuads(direction, it!!, ctx) }
    }

    override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, ctx: RenderContext) {
        emitQuads(null, sprite!!, ctx)
        if (overlays.isNotEmpty())
            overlays.forEach { emitQuads(null, it!!, ctx) }
    }

    protected fun emitQuads(facing: Direction?, sprite: Sprite, ctx: RenderContext) {
        ctx.emitter.run {
            draw(facing, Direction.UP, sprite)
            draw(facing, Direction.DOWN, sprite)
            draw(facing, Direction.NORTH, sprite)
            draw(facing, Direction.SOUTH, sprite)
            draw(facing, Direction.EAST, sprite)
            draw(facing, Direction.WEST, sprite)
        }
    }

    protected fun QuadEmitter.draw(
        facing: Direction?,
        side: Direction,
        sprite: Sprite,
        color: Int = -1
    ) {
        square(side, 0f, 0f, 1f, 1f, 0f)
        spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV)
        spriteColor(0,  color, color, color, color)
        if (emissives.contains(sprite))
            material(MATERIAL)
        val offset = if (side.axis.isVertical) side else when (facing) {
            Direction.SOUTH -> side.opposite
            Direction.WEST -> side.rotateYClockwise()
            Direction.EAST -> side.rotateYCounterclockwise()
            else -> side
        }
        val uv =
            if (sprite.width == 16 && sprite.height == 16) MachineTextureUV.FULL
            else MachineTextureUV.BY_DIRECTION[offset]!!
        sprite(0, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
        sprite(1, 0, sprite.getFrameU(uv.u1.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
        sprite(2, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v2.toDouble()))
        sprite(3, 0, sprite.getFrameU(uv.u2.toDouble()), sprite.getFrameV(uv.v1.toDouble()))
        emit()
    }

    enum class MachineTextureUV(val direction: Direction?, val u1: Float, val v1: Float, val u2: Float, val v2: Float) {
        FRONT(Direction.NORTH, 5.333f, 5.333f, 10.666f, 10.666f),
        LEFT(Direction.EAST, 0.0f, 5.333f, 5.332f, 10.666f),
        BACK(Direction.SOUTH, 10.667f, 10.667f, 16.0f, 16f),
        RIGHT(Direction.WEST, 10.667f, 5.333f, 16.0f, 10.665f),
        TOP(Direction.UP, 5.333f, 0.0f, 10.666f, 5.333f),
        BOTTOM(Direction.DOWN, 5.333f, 10.667f, 10.666f, 16.0f),
        FULL(null, 0f, 0f, 16f, 16f);

        companion object {
            val BY_DIRECTION = values().associateBy { it.direction }
        }
    }

    companion object {
        val MATERIAL by lazy {
            RendererAccess.INSTANCE.renderer?.materialFinder()!!.clear()
                .spriteDepth(1)
                .blendMode(0, BlendMode.CUTOUT)
                .disableAo(0, true)
                .disableDiffuse(0, true)
                .emissive(0, true)
                ?.find()
        }
        val TRANSFORM: ModelTransformation by lazy {
            MinecraftClient.getInstance().bakedModelManager.getModel(
                ModelIdentifier(Identifier("stone"), "")
            ).transformation
        }
    }
}